package dev.swiftnote

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * EditText that sees Enter/Shift+Enter/Esc/BACK before the IME does (Huawei Celia otherwise handles hardware Enter
 * inside the field and the key never reaches normal dispatch).
 */
class NoteEditText(
    context: Context,
    private val onEnter: () -> Unit,
    private val onEscape: () -> Unit,
    private val onActivity: () -> Unit,
) : EditText(context) {
    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        onActivity()
        when (event.keyCode) {
            KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK -> {
                if (event.action == KeyEvent.ACTION_UP) onEscape()
                return true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    if (event.isShiftPressed) insertNewline() else onEnter()
                }
                return true // DOWN repeats and UP are swallowed too
            }
        }
        return super.dispatchKeyEventPreIme(event)
    }

    private fun insertNewline() {
        val e = text ?: return
        val a = selectionStart.coerceAtLeast(0)
        val b = selectionEnd.coerceAtLeast(0)
        e.replace(minOf(a, b), maxOf(a, b), "\n")
    }
}

/**
 * The note box. Dumb view: it reports intent to the [Host] and never touches disk or media. The text is never logged.
 */
class NoteBox(
    context: Context,
    private val host: Host,
    header: String,
    draft: String,
) : LinearLayout(context) {

    interface Host {
        /** Enter without Shift. */
        fun onEnter(text: String)

        /** Esc or BACK: close without saving, keep a draft. */
        fun onEscape(text: String)

        /** [IDLE_MS] without a key, touch or text change. */
        fun onIdle(text: String)
    }

    private val edit: NoteEditText
    private val status = TextView(context)
    private val handler = Handler(Looper.getMainLooper())
    private val idle = Runnable { host.onIdle(text()) }

    init {
        orientation = VERTICAL
        defaultFocusHighlightEnabled = false
        val pad = dp(12)
        setPadding(pad, dp(8), pad, pad)
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(12).toFloat()
            setStroke(dp(2), Color.DKGRAY)
        }
        addView(
            TextView(context).apply {
                text = header
                setTextColor(Color.DKGRAY)
                textSize = 13f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            },
        )
        edit = NoteEditText(context, { host.onEnter(text()) }, { host.onEscape(text()) }, ::resetTimer).apply {
            defaultFocusHighlightEnabled = false
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 2
            maxLines = 5
            gravity = Gravity.TOP or Gravity.START
            setTextColor(Color.BLACK)
            setText(draft)
            setSelection(text.length)
        }
        addView(edit, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        status.apply {
            setTextColor(Color.RED)
            textSize = 12f
            visibility = GONE
        }
        addView(status)
        // IME text can arrive through the InputConnection without any KeyEvent.
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = resetTimer()
            override fun afterTextChanged(s: Editable?) {}
        })
        resetTimer()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    fun text(): String = edit.text.toString()

    /** Call once attached: takes input focus without a tap and asks for the IME explicitly. */
    fun focusEditor() {
        edit.requestFocus()
        try {
            context.getSystemService(InputMethodManager::class.java).showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Throwable) {
        }
    }

    /** Shows a short error under the field, or hides it for null. */
    fun showError(message: String?) {
        status.text = message ?: ""
        status.visibility = if (message == null) GONE else VISIBLE
    }

    fun resetTimer() {
        handler.removeCallbacks(idle)
        handler.postDelayed(idle, IDLE_MS)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        resetTimer()
        return super.dispatchTouchEvent(ev)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(idle)
        super.onDetachedFromWindow()
    }

    companion object {
        const val IDLE_MS = 120_000L
    }
}
