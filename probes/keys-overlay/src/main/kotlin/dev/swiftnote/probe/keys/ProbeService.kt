package dev.swiftnote.probe.keys

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

/**
 * Probe service. The key filter is off by default and on only while a YouTube window has input focus.
 * Only the exact Ctrl+Shift+N down/up pair is consumed. Every error fails open.
 */
class ProbeService : AccessibilityService() {
    private val main = Handler(Looper.getMainLooper())
    private val tracker = ShortcutTracker()
    private var overlayView: View? = null
    private var focusedPkg: String? = null

    /** Queued by the shortcut; re-checks that YouTube still has focus when it runs. */
    private val openRunnable = Runnable {
        if (ProbeState.serviceConnected && focusNow() == ProbeState.YOUTUBE) showBox()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ProbeState.service = this
        ProbeState.serviceConnected = true
        setFilter(false)
        ProbeState.logTransition("service connected")
        applyFocus(focusNow())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
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
        main.removeCallbacks(openRunnable)
        tracker.reset()
        setFilter(false)
        // Draft first, then the view goes away.
        (overlayView as? NoteBox)?.closeAsDraft("service $why")
        ProbeState.activeBox?.closeAsDraft("service $why")
        removeOverlay()
        ProbeState.boxOpen = false
        ProbeState.activeBox = null
        ProbeState.serviceConnected = false
        ProbeState.service = null
        ProbeState.logTransition("service gone: $why")
    }

    // ---- focus tracking and filter switching -------------------------------------------------------------------

    /** Package of the window with input focus right now, or null if none/unknown (an error fails open). */
    @Suppress("DEPRECATION")
    private fun focusNow(): String? {
        return try {
            val wins = windows
            val focused = wins?.firstOrNull { it.isFocused }
            val root = focused?.root
            val pkg = root?.packageName?.toString()
            root?.recycle()
            wins?.forEach { it.recycle() }
            pkg
        } catch (t: Throwable) {
            ProbeState.logTransition("focus lookup failed: ${t.javaClass.simpleName}")
            failOpen("focus lookup error")
            null
        }
    }

    private fun applyFocus(pkg: String?) {
        if (pkg != focusedPkg) {
            focusedPkg = pkg
            ProbeState.focusedPackage = pkg
            // Package names are allowed in logs (rule 3 forbids key codes, not packages).
            ProbeState.logTransition("focus -> ${pkg ?: "none"}")
            tracker.reset()
            main.removeCallbacks(openRunnable)
        }
        setFilter(pkg == ProbeState.YOUTUBE)
        // The user went to another app while an overlay box was up: keep the text as a draft.
        val v = overlayView
        if (v is NoteBox && pkg != null && pkg != ProbeState.YOUTUBE && pkg != packageName) v.closeAsDraft("focus left")
    }

    /** Filter off, key state forgotten, pending open dropped, open box closed as a draft. */
    private fun failOpen(why: String) {
        tracker.reset()
        main.removeCallbacks(openRunnable)
        setFilter(false)
        ProbeState.activeBox?.closeAsDraft("fail-open: $why")
    }

    /**
     * Switches the key filter. A failed switch is never recorded as a verified state: the state becomes unknown
     * and the next call retries even if the wanted value equals the last recorded one.
     */
    private fun setFilter(on: Boolean) {
        if (!on) tracker.reset()
        if (on == ProbeState.filterOn && !ProbeState.filterUnknown) return
        try {
            val info = serviceInfo ?: throw IllegalStateException("no serviceInfo")
            info.flags = if (on) {
                info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            } else {
                info.flags and AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS.inv()
            }
            serviceInfo = info
            ProbeState.filterOn = on
            ProbeState.filterUnknown = false
            ProbeState.logTransition("filter ${if (on) "ON" else "OFF"}")
        } catch (t: Throwable) {
            ProbeState.filterOn = false
            ProbeState.filterUnknown = true
            ProbeState.logTransition("filter switch failed (state unknown): ${t.javaClass.simpleName}")
        }
    }

    // ---- key handling -------------------------------------------------------------------------------------------

    override fun onKeyEvent(event: KeyEvent): Boolean {
        try {
            // Never trust the cached focus: re-check synchronously before logging or consuming anything.
            val pkg = focusNow()
            applyFocus(pkg)
            if (pkg != ProbeState.YOUTUBE) {
                // Outside YouTube (or unknown): count only, never the key code. Pass through.
                ProbeState.nonYoutubeKeyEvents++
                return false
            }
            ProbeState.logYoutubeKey(event.keyCode, event.action, event.metaState, event.repeatCount)
            return when (tracker.onKey(event.keyCode, event.action, event.metaState)) {
                Decision.PASS -> false
                Decision.CONSUME -> true
                Decision.CONSUME_AND_OPEN -> {
                    main.post(openRunnable)
                    true
                }
            }
        } catch (t: Throwable) {
            ProbeState.logTransition("onKeyEvent error: ${t.javaClass.simpleName}")
            failOpen("onKeyEvent error")
            return false
        }
    }

    // ---- overlay ------------------------------------------------------------------------------------------------

    private fun showBox() {
        if (ProbeState.boxOpen) return
        val path = ProbeState.path(this)
        ProbeState.logTransition("shortcut -> open box, path $path")
        try {
            when (path) {
                OverlayPath.A -> addOverlay(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
                OverlayPath.B -> {
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "Path B needs the overlay permission", Toast.LENGTH_LONG).show()
                        ProbeState.logTransition("path B: overlay permission missing")
                        return
                    }
                    addOverlay(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                }
                OverlayPath.C -> {
                    startActivity(Intent(this, NoteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        } catch (t: Throwable) {
            ProbeState.activeBox?.closeAsDraft("open failed")
            removeOverlay()
            ProbeState.logTransition("open box failed: ${t.javaClass.simpleName}")
        }
    }

    private fun addOverlay(type: Int) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val box = NoteBox(this) { removeOverlay() }
        val d = resources.displayMetrics.density
        // No FLAG_NOT_FOCUSABLE: the box must take keyboard input without a tap. Touches outside go to YouTube.
        val lp = WindowManager.LayoutParams(
            (560 * d).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        )
        lp.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        lp.y = (48 * d).toInt()
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        wm.addView(box, lp)
        overlayView = box
        box.post { box.focusEditor() }
    }

    private fun removeOverlay() {
        val v = overlayView ?: return
        overlayView = null
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(v)
        } catch (_: Throwable) {
        }
    }
}
