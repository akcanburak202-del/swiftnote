package dev.swiftnote.probe.keys

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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
        // IME text can arrive through InputConnection without any KeyEvent: typing also resets the idle timer.
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = resetTimer()
            override fun afterTextChanged(s: Editable?) {}
        })
        ProbeState.boxOpen = true
        ProbeState.activeBox = this
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

    /**
     * Close keeping any text as a draft (focus lost, service gone, activity stopped).
     * Returns true if the box is closed and the text is on disk; false if the write failed and the box stays open.
     */
    fun closeAsDraft(reason: String): Boolean = finish(save = false, reason = reason)

    private fun finish(save: Boolean, reason: String): Boolean {
        if (finished) return true
        val text = edit.text.toString()
        // Write to disk first (rule 2). The probe has no note file: "last saved note" in prefs stands in for it.
        val ok = if (save && text.isNotBlank()) {
            ProbeState.setLastNote(context, text) && ProbeState.setDraft(context, "")
        } else if (save) {
            ProbeState.setDraft(context, "")
        } else {
            ProbeState.setDraft(context, if (text.isBlank()) "" else text)
        }
        if (!ok) {
            status.text = "Write failed, text kept. Press Enter to retry."
            resetTimer()
            return false
        }
        finished = true
        handler.removeCallbacks(timeout)
        ProbeState.boxOpen = false
        if (ProbeState.activeBox === this) ProbeState.activeBox = null
        ProbeState.logTransition("box closed: $reason, chars=${text.length}")
        onFinished(reason)
        return true
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
