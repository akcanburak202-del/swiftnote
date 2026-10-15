package dev.swiftnote

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class ShortcutTrackerTest {
    private val cs = KeyEvent.META_ALT_ON
    private val n = KeyEvent.KEYCODE_N
    private val down = KeyEvent.ACTION_DOWN
    private val up = KeyEvent.ACTION_UP

    @Test fun fullPressOpensAndConsumesBothHalves() {
        val t = ShortcutTracker()
        assertEquals(Decision.CONSUME, t.onKey(n, down, cs, 0))
        assertEquals(Decision.CONSUME, t.onKey(n, down, cs, 1)) // repeat
        assertEquals(Decision.CONSUME_AND_OPEN, t.onKey(n, up, 0, 0)) // Alt already released
        assertEquals(Decision.PASS, t.onKey(n, down, 0, 0))
    }

    @Test fun unpairedUpPasses() {
        assertEquals(Decision.PASS, ShortcutTracker().onKey(n, up, 0, 0))
    }

    @Test fun resetDropsHalfPress() {
        val t = ShortcutTracker()
        t.onKey(n, down, cs, 0)
        t.reset()
        assertEquals(Decision.PASS, t.onKey(n, up, 0, 0))
        assertEquals(Decision.PASS, t.onKey(n, down, 0, 0))
    }

    @Test fun otherKeysAndPlainNPass() {
        val t = ShortcutTracker()
        assertEquals(Decision.PASS, t.onKey(KeyEvent.KEYCODE_A, down, cs, 0))
        assertEquals(Decision.PASS, t.onKey(n, down, KeyEvent.META_SHIFT_ON, 0))
    }

    @Test fun repeatDownGainingModifiersPasses() {
        val t = ShortcutTracker()
        assertEquals(Decision.PASS, t.onKey(n, down, cs, 3))
        assertEquals(Decision.PASS, t.onKey(n, up, 0, 0))
    }
}
