package dev.swiftnote

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutMatcherTest {
    private val alt = KeyEvent.META_ALT_ON
    private val n = KeyEvent.KEYCODE_N

    @Test fun altNMatches() = assertTrue(ShortcutMatcher.matches(n, alt))

    @Test fun sideSpecificAndRightAltMatch() {
        assertTrue(ShortcutMatcher.matches(n, alt or KeyEvent.META_ALT_LEFT_ON))
        assertTrue(ShortcutMatcher.matches(n, alt or KeyEvent.META_ALT_RIGHT_ON))
    }

    @Test fun numLockDoesNotBreakMatch() = assertTrue(ShortcutMatcher.matches(n, alt or 0x200000))

    @Test fun missingAltDoesNotMatch() {
        assertFalse(ShortcutMatcher.matches(n, 0))
        assertFalse(ShortcutMatcher.matches(n, KeyEvent.META_SHIFT_ON))
    }

    @Test fun extraModifierDoesNotMatch() {
        assertFalse(ShortcutMatcher.matches(n, alt or KeyEvent.META_SHIFT_ON))
        assertFalse(ShortcutMatcher.matches(n, alt or KeyEvent.META_CTRL_ON))
        assertFalse(ShortcutMatcher.matches(n, alt or KeyEvent.META_META_ON))
    }

    @Test fun oldCtrlShiftNDoesNotMatch() =
        assertFalse(ShortcutMatcher.matches(n, KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON))

    @Test fun otherKeyDoesNotMatch() = assertFalse(ShortcutMatcher.matches(KeyEvent.KEYCODE_M, alt))
}
