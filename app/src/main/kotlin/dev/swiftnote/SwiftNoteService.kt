package dev.swiftnote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

/**
 * The shortcut service. The key filter is off by default and on only while a YouTube window has input focus; only the
 * exact Ctrl+Shift+N down/up pair is consumed, every error fails open (AGENTS.md rule 1). Key codes and note text are
 * never logged. Disk work runs on [io]; the key callback never waits for it.
 */
class SwiftNoteService : AccessibilityService() {
    private val main = Handler(Looper.getMainLooper())
    private val tracker = ShortcutTracker()
    private lateinit var io: ExecutorService
    private lateinit var drafts: DraftStore
    private lateinit var media: MediaSource
    private lateinit var writer: NoteWriter

    private var focusedPkg: String? = null
    private var filterOn = false
    private var filterUnknown = false
    private var down = false

    /** Window id -> package, learned from TYPE_WINDOW_STATE_CHANGED. Unknown ids are treated as not YouTube. */
    private val windowPkg = HashMap<Int, String>()

    /** What the open box belongs to. Null when no box is open. */
    private class Session(
        val title: String?,
        val channel: String?,
        val time: String?,
        val pausedByUs: Boolean,
    ) {
        /** A save or draft write is in flight: further input and close requests are ignored. */
        var busy = false

        /** Set on the io thread once the note is on disk. */
        @Volatile var noteWritten = false
    }

    private var box: NoteBox? = null
    private var session: Session? = null

    private val openRunnable = Runnable {
        if (!down && connected && focusNow() == YOUTUBE) openBox()
    }

    override fun onCreate() {
        super.onCreate()
        io = Executors.newSingleThreadExecutor { r -> Thread(r, "swiftnote-io") }
        drafts = DraftStore(this)
        media = MediaSource(this)
        val dir = File(Environment.getExternalStorageDirectory(), NOTES_DIR)
        writer = NoteWriter(dir)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        connected = true
        setFilter(false)
        try {
            io.execute { drafts.current() } // load off the main thread before the first shortcut
        } catch (_: RejectedExecutionException) {
        }
        applyFocus(focusNow())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val id = event?.windowId ?: -1
            val pkg = event?.packageName?.toString()
            if (id != -1 && pkg != null && event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                windowPkg[id] = pkg
                // Another app's window appearing closes an open box (draft kept).
                if (pkg != YOUTUBE && pkg != packageName && pkg != imePackage()) closeBoxLater("window event")
            }
        } catch (_: Throwable) {
        }
        applyFocus(focusNow())
    }

    override fun onInterrupt() {
        failOpen("interrupt")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        shutdown("unbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        shutdown("destroy")
        super.onDestroy()
    }

    private fun shutdown(why: String) {
        if (down) return
        down = true
        connected = false
        main.removeCallbacksAndMessages(null)
        tracker.reset()
        trySwitch(false)
        val s = session
        val b = box
        io.shutdown()
        try {
            io.awaitTermination(1500, TimeUnit.MILLISECONDS)
        } catch (_: InterruptedException) {
        }
        // Last chance for the text: a synchronous write is acceptable here (the service is going away).
        if (s != null && b != null && !(s.busy && s.noteWritten)) {
            if (!drafts.save(b.text())) Log.e(TAG, "draft not stored at shutdown")
        }
        removeOverlay()
        Log.w(TAG, "service gone: $why")
    }

    // ---- focus tracking and filter switching -------------------------------------------------------------------

    private class WinInfo(val id: Int, val type: Int, val layer: Int)

    private class Scan(val focusedId: Int, val infos: List<WinInfo>)

    /** Window list from system_server only; never touches AccessibilityNodeInfo (a synchronous IPC into the app). */
    @Suppress("DEPRECATION")
    private fun scanWindows(): Scan {
        var focusedId = -1
        val infos = ArrayList<WinInfo>()
        windows?.forEach {
            if (it.isFocused) focusedId = it.id
            infos.add(WinInfo(it.id, it.type, it.layer))
            it.recycle()
        }
        windowPkg.keys.retainAll(infos.map { it.id }.toSet())
        return Scan(focusedId, infos)
    }

    /** Package of the window with input focus now, or null if none/unknown. An error fails open. */
    private fun focusNow(): String? = try {
        val scan = scanWindows()
        if (box != null) checkUnderlying(scan.infos)
        if (scan.focusedId == -1) null else windowPkg[scan.focusedId]
    } catch (t: Throwable) {
        Log.w(TAG, "focus lookup failed: ${t.javaClass.simpleName}")
        failOpen("focus lookup error")
        null
    }

    /** Topmost real application window that is not ours (skips IME, accessibility overlay, system windows). */
    private fun underlyingPkg(infos: List<WinInfo>): String? {
        for (w in infos.sortedByDescending { it.layer }) {
            if (w.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue
            val pkg = windowPkg[w.id]
            if (pkg == packageName) continue
            return pkg
        }
        return null
    }

    /** While a box is open the app under it must be known YouTube; anything else closes the box as a draft. */
    private fun checkUnderlying(infos: List<WinInfo>) {
        if (underlyingPkg(infos) != YOUTUBE) closeBoxLater("underlying app is not YouTube")
    }

    private fun imePackage(): String? = try {
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.substringBefore('/')
    } catch (_: Throwable) {
        null
    }

    private fun applyFocus(pkg: String?) {
        if (pkg != focusedPkg) {
            focusedPkg = pkg
            tracker.reset()
            main.removeCallbacks(openRunnable)
        }
        setFilter(pkg == YOUTUBE)
        if (pkg != null && pkg != YOUTUBE && pkg != packageName) closeBoxLater("focus left")
    }

    /** Filter off, key state forgotten, pending open dropped, open box closed as a draft. Playback is left alone. */
    private fun failOpen(why: String) {
        tracker.reset()
        main.removeCallbacks(openRunnable)
        setFilter(false)
        closeBoxLater("fail-open: $why")
    }

    /**
     * Switches the key filter. A failed switch is never recorded as verified: the state becomes unknown and the next
     * call retries. If the filter cannot be turned off, the service switches itself off.
     */
    private fun setFilter(on: Boolean) {
        if (!on) tracker.reset()
        if (on == filterOn && !filterUnknown) return
        if (trySwitch(on) || trySwitch(on)) return
        filterOn = false
        filterUnknown = true
        if (on) {
            setFilter(false)
        } else {
            Log.e(TAG, "filter OFF failed twice: disableSelf()")
            disableSelf()
        }
    }

    private fun trySwitch(on: Boolean): Boolean = try {
        val info = serviceInfo ?: throw IllegalStateException("no serviceInfo")
        info.flags = if (on) {
            info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        } else {
            info.flags and AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS.inv()
        }
        serviceInfo = info
        filterOn = on
        filterUnknown = false
        true
    } catch (t: Throwable) {
        Log.w(TAG, "filter switch failed: ${t.javaClass.simpleName}")
        false
    }

    // ---- key handling -------------------------------------------------------------------------------------------

    override fun onKeyEvent(event: KeyEvent): Boolean {
        try {
            // Never trust the cached focus: re-check before consuming anything.
            val pkg = focusNow()
            applyFocus(pkg)
            if (pkg != YOUTUBE) return false
            return when (tracker.onKey(event.keyCode, event.action, event.metaState, event.repeatCount)) {
                Decision.PASS -> false
                Decision.CONSUME -> true
                Decision.CONSUME_AND_OPEN -> {
                    main.post(openRunnable)
                    true
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "onKeyEvent error: ${t.javaClass.simpleName}")
            failOpen("onKeyEvent error")
            return false
        }
    }

    // ---- the note box -------------------------------------------------------------------------------------------

    private fun openBox() {
        if (box != null) return
        val snap = try {
            media.snapshot()
        } catch (_: Throwable) {
            null
        }
        var paused = false
        if (snap != null && snap.playing) {
            paused = try {
                snap.controller.transportControls.pause()
                true
            } catch (_: Throwable) {
                false
            }
        }
        val title = snap?.title
        val channel = if (title != null) snap?.channel else null
        val time = if (title != null) snap?.positionMs?.let { NoteFormat.formatTime(it) } else null
        val s = Session(title, channel, time, paused)
        val header = buildString {
            append("▶ ").append(title ?: NoteFormat.UNTITLED_TITLE)
            if (channel != null) append(" · ").append(channel)
            if (time != null) append(" @ ").append(time)
        }
        try {
            val b = NoteBox(this, boxHost, header, drafts.current())
            addOverlay(b)
            box = b
            session = s
        } catch (t: Throwable) {
            Log.e(TAG, "open box failed: ${t.javaClass.simpleName}")
            removeOverlay()
            if (paused) media.resume()
            failOpen("open box failed")
        }
    }

    private val boxHost = object : NoteBox.Host {
        override fun onEnter(text: String) = submit(text)
        override fun onEscape(text: String) = closeWithDraft(text, "esc/back", resume = true, force = false)
        override fun onIdle(text: String) = closeWithDraft(text, "idle timeout", resume = true, force = true)
    }

    private fun closeBoxLater(reason: String) {
        if (box == null) return
        main.post {
            val b = box ?: return@post
            closeWithDraft(b.text(), reason, resume = false, force = true)
        }
    }

    /** Enter: save the note (empty text just closes). The disk write runs on [io]; the box waits for the result. */
    private fun submit(text: String) {
        val s = session ?: return
        if (s.busy) return
        if (text.isBlank()) {
            closeWithDraft("", "enter on empty", resume = true, force = false)
            return
        }
        s.busy = true
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        try {
            io.execute {
                val r = writer.append(s.title, s.channel, s.time, text, date)
                if (r.isSuccess) {
                    s.noteWritten = true
                    if (!drafts.save("")) Log.w(TAG, "old draft not cleared after save")
                }
                main.post { onSaveResult(s, text, r) }
            }
        } catch (_: RejectedExecutionException) {
            s.busy = false
        }
    }

    private fun onSaveResult(s: Session, text: String, r: Result<File>) {
        if (session !== s) return
        val b = box ?: return
        if (r.isFailure) {
            Log.e(TAG, "note write failed: ${r.exceptionOrNull()?.javaClass?.simpleName}")
            s.busy = false
            b.showError(
                if (!Environment.isExternalStorageManager()) "Kayıt başarısız: tüm dosyalara erişim izni yok. Metin duruyor."
                else "Kayıt başarısız (${r.exceptionOrNull()?.javaClass?.simpleName}). Metin duruyor, Enter ile tekrar dene.",
            )
            return
        }
        try {
            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("SwiftNote", NoteFormat.clipboardText(s.time, text)))
        } catch (t: Throwable) {
            Log.w(TAG, "clipboard failed: ${t.javaClass.simpleName}")
        }
        r.getOrNull()?.name?.let {
            getSharedPreferences("app", Context.MODE_PRIVATE).edit().putString(KEY_LAST_FILE, it).apply()
        }
        finishBox(s, resume = true, reason = "saved")
    }

    /**
     * Close keeping [text] as the draft (blank clears it). The write goes to [io]; the box closes after it. When the
     * write fails and the close is not [force]d (Esc), the box stays open with the error.
     */
    private fun closeWithDraft(text: String, reason: String, resume: Boolean, force: Boolean) {
        val s = session ?: return
        if (s.busy) return
        s.busy = true
        val ok = try {
            io.execute {
                val stored = drafts.save(text)
                main.post { onDraftResult(s, stored, resume, force, reason) }
            }
            return
        } catch (_: RejectedExecutionException) {
            drafts.save(text) // shutting down: the only path left
        }
        onDraftResult(s, ok, resume, force, reason)
    }

    private fun onDraftResult(s: Session, stored: Boolean, resume: Boolean, force: Boolean, reason: String) {
        if (session !== s) return
        if (!stored && !force) {
            s.busy = false
            box?.showError("Taslak yazılamadı. Metin duruyor.")
            return
        }
        if (!stored) Log.e(TAG, "draft not stored on forced close")
        finishBox(s, resume, reason)
    }

    /** Removes the box, then resumes playback if we paused it and YouTube is still the app underneath. */
    private fun finishBox(s: Session, resume: Boolean, reason: String) {
        val resumeNow = resume && s.pausedByUs && try {
            underlyingPkg(scanWindows().infos) == YOUTUBE
        } catch (_: Throwable) {
            false
        }
        removeOverlay()
        Log.w(TAG, "box closed: $reason")
        if (resumeNow) media.resume()
        applyFocus(focusNow())
    }

    private fun addOverlay(view: NoteBox) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val d = resources.displayMetrics.density
        // No FLAG_NOT_FOCUSABLE: the box must take keyboard input without a tap. Touches outside go to YouTube.
        val lp = WindowManager.LayoutParams(
            (560 * d).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        )
        lp.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        lp.y = (48 * d).toInt()
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        wm.addView(view, lp)
        view.post { view.focusEditor() }
    }

    private fun removeOverlay() {
        val v = box
        box = null
        session = null
        if (v == null) return
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(v)
        } catch (_: Throwable) {
        }
    }

    companion object {
        const val YOUTUBE = "com.google.android.youtube"
        const val TAG = "SwiftNote"
        const val NOTES_DIR = "MateBridge/Wi-Fi/Video Notları"
        const val KEY_LAST_FILE = "lastFile"

        /** True while the service is connected (the setup screen reads it). */
        @Volatile var connected = false
    }
}
