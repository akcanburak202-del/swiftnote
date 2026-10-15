package dev.swiftnote.probe.keys

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * The note box, shared by overlay paths A, B and C. Enter closes, Shift+Enter inserts a newline, Esc/BACK closes
 * keeping a draft, and 60 s without any key or touch closes keeping a draft. The text is never logged.
 */
class NoteBox(
    context: Context,
    private val onFinished: (reason: String) -> Unit,
) : LinearLayout(context) {

    private val edit = EditText(context)
    private val status = TextView(context)
    private val handler = Handler(Looper.getMainLooper())
    private var finished = false
    private val timeout = Runnable { finish(save = false, reason = "timeout") }

    init {
        orientation = VERTICAL
        val pad = dp(12)
        setPadding(pad, pad, pad, pad)
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(12).toFloat()
            setStroke(dp(2), Color.DKGRAY)
        }
        val title = TextView(context).apply {
            text = "Probe note box: Enter = close, Shift+Enter = newline, Esc = draft"
            setTextColor(Color.DKGRAY)
            textSize = 12f
        }
        addView(title)
        edit.apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 3
            maxLines = 6
            gravity = Gravity.TOP or Gravity.START
            setTextColor(Color.BLACK)
            setText(ProbeState.draft(context))
            setSelection(text.length)
        }
        addView(edit, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        status.apply {
            setTextColor(Color.RED)
            textSize = 12f
        }
        addView(status)
        ProbeState.boxOpen = true
        resetTimer()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    /** Call once the view is attached so the EditText takes input focus without a tap. */
    fun focusEditor() {
        edit.requestFocus()
    }

    fun resetTimer() {
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, 60_000L)
    }

    /** Close keeping any text as a draft (focus lost, service gone, activity stopped). */
    fun closeAsDraft(reason: String) = finish(save = false, reason = reason)

    private fun finish(save: Boolean, reason: String) {
        if (finished) return
        val text = edit.text.toString()
        // Write to disk first (rule 2). Saved note: probe has no file, so clearing the draft stands in for it.
        val keep = if (save) "" else if (text.isBlank()) "" else text
        if (!ProbeState.setDraft(context, keep)) {
            status.text = "Write failed, text kept. Press Enter to retry."
            resetTimer()
            return
        }
        finished = true
        handler.removeCallbacks(timeout)
        ProbeState.boxOpen = false
        ProbeState.logTransition("box closed: $reason, chars=${text.length}")
        onFinished(reason)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        resetTimer()
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        resetTimer()
        when (event.keyCode) {
            KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK -> {
                if (event.action == KeyEvent.ACTION_UP) finish(save = false, reason = "esc/back")
                return true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (!event.isShiftPressed) {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        finish(save = true, reason = "enter")
                    }
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(timeout)
        super.onDetachedFromWindow()
    }
}
