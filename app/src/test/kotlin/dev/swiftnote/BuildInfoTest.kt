package dev.swiftnote

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildInfoTest {
    @Test
    fun describeShowsShaAndTime() {
        assertEquals("SwiftNote\nbuild abc1234\n2026-10-14T12:00Z", BuildInfo.describe("abc1234", "2026-10-14T12:00Z"))
    }
}
