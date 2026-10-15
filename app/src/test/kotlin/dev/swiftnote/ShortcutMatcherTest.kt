package dev.swiftnote

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutMatcherTest {
    private val cs = KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON

    @Test fun exactShortcutMatches() = assertTrue(ShortcutMatcher.matches(KeyEvent.KEYCODE_N, cs))

    @Test fun sideSpecificBitsStillMatch() = assertTrue(
        ShortcutMatcher.matches(KeyEvent.KEYCODE_N, cs or KeyEvent.META_CTRL_LEFT_ON or KeyEvent.META_SHIFT_RIGHT_ON),
    )

    @Test fun missingModifierDoesNotMatch() {
        assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_N, KeyEvent.META_CTRL_ON))
        assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_N, KeyEvent.META_SHIFT_ON))
        assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_N, 0))
    }

    @Test fun extraModifierDoesNotMatch() {
        assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_N, cs or KeyEvent.META_ALT_ON))
        assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_N, cs or KeyEvent.META_META_ON))
    }

    @Test fun otherKeyDoesNotMatch() = assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_M, cs))
}
