package dev.swiftnote

import android.view.KeyEvent

enum class Decision { PASS, CONSUME, CONSUME_AND_OPEN }

/** Pure down/up pairing for the shortcut: an UP is consumed only if its DOWN was consumed. */
class ShortcutTracker {
    private var held = false

    fun onKey(keyCode: Int, action: Int, metaState: Int, repeatCount: Int): Decision {
        if (keyCode != KeyEvent.KEYCODE_N) return Decision.PASS
        return when (action) {
            KeyEvent.ACTION_DOWN -> when {
                held -> Decision.CONSUME // auto-repeat of the consumed press
                repeatCount == 0 && ShortcutMatcher.matches(keyCode, metaState) -> {
                    held = true
                    Decision.CONSUME
                }
                else -> Decision.PASS
            }
            KeyEvent.ACTION_UP -> if (held) {
                held = false
                Decision.CONSUME_AND_OPEN // open only once the consumed pair is complete
            } else {
                Decision.PASS
            }
            else -> Decision.PASS
        }
    }

    /** Filter turned off or focus changed: forget the half-finished press. */
    fun reset() {
        held = false
    }
}
