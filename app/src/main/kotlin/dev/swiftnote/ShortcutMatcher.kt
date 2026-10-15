package dev.swiftnote

import android.view.KeyEvent

/** Pure key matching: exactly Ctrl+Shift+N, no Alt, no Meta. */
object ShortcutMatcher {
    private const val WANTED = KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
    private const val FORBIDDEN = KeyEvent.META_ALT_ON or KeyEvent.META_META_ON

    fun matches(keyCode: Int, metaState: Int): Boolean =
        keyCode == KeyEvent.KEYCODE_N &&
            (metaState and WANTED) == WANTED &&
            (metaState and FORBIDDEN) == 0
}
