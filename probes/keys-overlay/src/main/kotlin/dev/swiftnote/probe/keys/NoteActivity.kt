package dev.swiftnote.probe.keys

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout

/** Overlay path C: transparent activity hosting the same note box. */
class NoteActivity : Activity() {
    private var box: NoteBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = NoteBox(this) { finish() }
        box = b
        val root = FrameLayout(this)
        val lp = FrameLayout.LayoutParams(
            (560 * resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        )
        lp.bottomMargin = (48 * resources.displayMetrics.density).toInt()
        root.addView(b, lp)
        setContentView(root)
        ProbeState.logTransition("path C activity created")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) box?.focusEditor()
    }

    @Deprecated("Deprecated in API 33, still delivered on API 31")
    override fun onBackPressed() {
        box?.closeAsDraft("back") ?: finish()
    }

    override fun onStop() {
        // Fail open: if the user leaves (PiP, recents), keep the text as a draft and go away.
        // Finish only if the text is on disk; otherwise the box stays up with an error.
        val saved = box?.closeAsDraft("activity stopped") ?: true
        if (saved && !isFinishing) finish()
        super.onStop()
    }
}
