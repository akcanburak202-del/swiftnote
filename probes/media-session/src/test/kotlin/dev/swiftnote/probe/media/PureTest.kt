package dev.swiftnote.probe.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PureTest {
    @Test fun time() {
        assertEquals("00:00", TimeFormat.format(-5))
        assertEquals("01:05", TimeFormat.format(65_999))
        assertEquals("1:01:01", TimeFormat.format(3_661_000))
    }

    @Test fun ids() {
        assertEquals("dQw4w9WgXcQ", VideoId.fromText("https://youtu.be/dQw4w9WgXcQ?si=abc"))
        assertEquals("dQw4w9WgXcQ", VideoId.fromText("Title https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=5"))
        assertEquals("dQw4w9WgXcQ", VideoId.fromText("https://youtube.com/shorts/dQw4w9WgXcQ"))
        assertNull(VideoId.fromText("no link"))
        assertNull(VideoId.fromText(null))
        assertTrue(VideoId.isId("dQw4w9WgXcQ"))
        assertFalse(VideoId.isId("short"))
    }
}
