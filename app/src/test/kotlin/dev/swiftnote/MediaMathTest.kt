package dev.swiftnote

import android.media.session.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMathTest {
    private val playing = PlaybackState.STATE_PLAYING
    private val paused = PlaybackState.STATE_PAUSED

    @Test fun playingExtrapolatesBySpeed() {
        assertEquals(10_000L + 2_000L, MediaMath.estimatePosition(playing, 10_000, 1_000, 3_000, 1f))
        assertEquals(10_000L + 4_000L, MediaMath.estimatePosition(playing, 10_000, 1_000, 3_000, 2f))
    }

    @Test fun pausedKeepsRawPosition() {
        assertEquals(10_000L, MediaMath.estimatePosition(paused, 10_000, 1_000, 99_000, 1f))
    }

    @Test fun unknownPositionIsNull() {
        assertNull(MediaMath.estimatePosition(playing, -1, 1_000, 3_000, 1f))
    }

    @Test fun oddInputsDoNotGoBackwards() {
        assertEquals(5_000L, MediaMath.estimatePosition(playing, 5_000, 9_000, 3_000, 1f)) // clock skew
        assertEquals(5_000L, MediaMath.estimatePosition(playing, 5_000, 0, 3_000, 1f)) // no update time
        assertEquals(7_000L, MediaMath.estimatePosition(playing, 5_000, 1_000, 3_000, 0f)) // speed 0 -> 1
    }

    @Test fun playingStates() {
        assertTrue(MediaMath.isPlaying(playing))
        assertTrue(MediaMath.isPlaying(PlaybackState.STATE_BUFFERING))
        assertFalse(MediaMath.isPlaying(paused))
        assertFalse(MediaMath.isPlaying(PlaybackState.STATE_NONE))
    }

    @Test fun channelFallbackOrder() {
        assertEquals("A", MediaMath.pickChannel(" A ", "B", "C"))
        assertEquals("B", MediaMath.pickChannel(null, "B", "C"))
        assertEquals("C", MediaMath.pickChannel("", " ", "C"))
        assertNull(MediaMath.pickChannel(null, null, null))
    }

    @Test fun componentListMatchesShortAndFullNames() {
        val l = "a.b/a.b.Svc:dev.swiftnote/.SwiftNoteService:x/y"
        assertTrue(ComponentList.contains(l, "dev.swiftnote", "dev.swiftnote.SwiftNoteService"))
        assertTrue(ComponentList.contains(l, "a.b", "a.b.Svc"))
        assertFalse(ComponentList.contains(l, "dev.swiftnote", "dev.swiftnote.Other"))
        assertFalse(ComponentList.contains(null, "a", "b"))
        assertFalse(
            ComponentList.contains("dev.swiftnote.probe.keys/.ProbeService", "dev.swiftnote", "dev.swiftnote.ProbeService"),
        )
    }
}
