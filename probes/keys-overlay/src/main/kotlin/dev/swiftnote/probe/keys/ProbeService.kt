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
    private var consumedDown = false
    private var overlayView: View? = null
    private var focusedPkg: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ProbeState.service = this
        ProbeState.serviceConnected = true
        setFilter(false)
        ProbeState.logTransition("service connected")
        evaluateFocus()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        evaluateFocus()
    }

    override fun onInterrupt() {
        setFilter(false)
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
        setFilter(false)
        removeOverlay()
        ProbeState.serviceConnected = false
        ProbeState.service = null
        ProbeState.logTransition("service gone: $why")
    }

    // ---- focus tracking and filter switching -------------------------------------------------------------------

    @Suppress("DEPRECATION")
    private fun evaluateFocus() {
        var pkg: String? = null
        try {
            val wins = windows
            val focused = wins?.firstOrNull { it.isFocused }
            val root = focused?.root
            pkg = root?.packageName?.toString()
            root?.recycle()
            wins?.forEach { it.recycle() }
        } catch (t: Throwable) {
            ProbeState.logTransition("focus lookup failed: ${t.javaClass.simpleName}")
        }
        if (pkg != focusedPkg) {
            focusedPkg = pkg
            ProbeState.focusedPackage = pkg
            // Package names of the focused window are allowed in logs (rule 3 forbids key codes, not packages).
            ProbeState.logTransition("focus -> ${pkg ?: "none"}")
        }
        setFilter(pkg == ProbeState.YOUTUBE)
        // Fail open: the user went to another app while an overlay box was up; keep the text as a draft.
        val v = overlayView
        if (v is NoteBox && pkg != null && pkg != ProbeState.YOUTUBE && pkg != packageName) v.closeAsDraft("focus left")
    }

    private fun setFilter(on: Boolean) {
        if (on == ProbeState.filterOn) return
        try {
            val info = serviceInfo ?: return
            info.flags = if (on) {
                info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            } else {
                info.flags and AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS.inv()
            }
            serviceInfo = info
            ProbeState.filterOn = on
            ProbeState.logTransition("filter ${if (on) "ON" else "OFF"}")
        } catch (t: Throwable) {
            ProbeState.filterOn = false
            ProbeState.logTransition("filter switch failed: ${t.javaClass.simpleName}")
        }
    }

    // ---- key handling -------------------------------------------------------------------------------------------

    override fun onKeyEvent(event: KeyEvent): Boolean {
        try {
            if (!ProbeState.filterOn || focusedPkg != ProbeState.YOUTUBE) {
                // Outside YouTube: count only, never the key code. Re-check focus, then pass through.
                ProbeState.nonYoutubeKeyEvents++
                evaluateFocus()
                return false
            }
            ProbeState.logYoutubeKey(event.keyCode, event.action, event.metaState, event.repeatCount)
            if (event.keyCode != KeyEvent.KEYCODE_N) return false
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (consumedDown || ShortcutMatcher.matches(event.keyCode, event.metaState)) {
                        if (!consumedDown) main.post { showBox() }
                        consumedDown = true
                        return true
                    }
                }
                KeyEvent.ACTION_UP -> {
                    if (consumedDown) {
                        consumedDown = false
                        return true
                    }
                }
            }
            return false
        } catch (t: Throwable) {
            consumedDown = false
            setFilter(false)
            ProbeState.logTransition("onKeyEvent error: ${t.javaClass.simpleName}")
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
            ProbeState.boxOpen = false
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
        ProbeState.boxOpen = false
    }
}
