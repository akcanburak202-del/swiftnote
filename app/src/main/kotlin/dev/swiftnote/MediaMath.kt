package dev.swiftnote

import android.media.session.PlaybackState

/** Pure helpers for reading a YouTube media session. */
object MediaMath {
    /** Counts as "playing" for the pause decision (a buffering video would start again by itself). */
    fun isPlaying(state: Int): Boolean =
        state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING

    /**
     * Current position: `position + (now - lastUpdate) * speed` while playing, the raw position otherwise.
     * Null when the position is unknown. [now] and [lastUpdateTime] are both `elapsedRealtime` milliseconds.
     */
    fun estimatePosition(state: Int, position: Long, lastUpdateTime: Long, now: Long, speed: Float): Long? {
        if (position < 0) return null // PLAYBACK_POSITION_UNKNOWN
        if (state != PlaybackState.STATE_PLAYING || lastUpdateTime <= 0) return position
        val age = maxOf(now - lastUpdateTime, 0L)
        val s = if (speed > 0f) speed else 1f
        return position + (age * s).toLong()
    }

    /** First non-blank of artist, album artist, author. */
    fun pickChannel(artist: String?, albumArtist: String?, author: String?): String? =
        listOf(artist, albumArtist, author).firstOrNull { !it.isNullOrBlank() }?.trim()
}

/** Pure check of the secure-setting lists of enabled accessibility services / notification listeners. */
object ComponentList {
    /** True if [list] (`pkg/cls:pkg/.cls:...`, short or full class names) names exactly [pkg]/[cls]. */
    fun contains(list: String?, pkg: String, cls: String): Boolean {
        if (list.isNullOrEmpty()) return false
        return list.split(':').any { entry ->
            val slash = entry.indexOf('/')
            if (slash <= 0) return@any false
            val p = entry.substring(0, slash)
            val c = entry.substring(slash + 1)
            val full = if (c.startsWith(".")) p + c else c
            p == pkg && full == cls
        }
    }
}
