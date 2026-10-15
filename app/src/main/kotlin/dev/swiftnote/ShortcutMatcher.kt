package dev.swiftnote

import android.view.KeyEvent

/** Pure key matching: exactly Alt+N (left or right Alt), no Ctrl, Shift or Meta. */
object ShortcutMatcher {
    private const val FORBIDDEN = KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON or KeyEvent.META_META_ON

    fun matches(keyCode: Int, metaState: Int): Boolean =
        keyCode == KeyEvent.KEYCODE_N &&
            (metaState and KeyEvent.META_ALT_ON) != 0 &&
            (metaState and FORBIDDEN) == 0
}
