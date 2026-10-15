package dev.swiftnote

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.service.notification.NotificationListenerService

/**
 * Exists only so MediaSessionManager.getActiveSessions accepts our ComponentName (needs notification access).
 * Deliberately ignores every notification.
 */
class MediaAccessListener : NotificationListenerService()

/** Reads and controls the YouTube media session. Every call may fail (no access, no session): callers get null. */
class MediaSource(private val context: Context) {
    class Snapshot(
        val title: String?,
        val channel: String?,
        /** Playback position at the time of the snapshot, or null if unknown. */
        val positionMs: Long?,
        val playing: Boolean,
        val controller: MediaController,
    )

    /** The YouTube session (the playing one if there are several), or null without access / session. */
    fun controller(): MediaController? = try {
        val all = context.getSystemService(MediaSessionManager::class.java)
            .getActiveSessions(ComponentName(context, MediaAccessListener::class.java))
            .filter { it.packageName == SwiftNoteService.YOUTUBE }
        all.firstOrNull { MediaMath.isPlaying(it.playbackState?.state ?: PlaybackState.STATE_NONE) } ?: all.firstOrNull()
    } catch (_: SecurityException) {
        null
    } catch (_: Throwable) {
        null
    }

    fun snapshot(): Snapshot? {
        val c = controller() ?: return null
        val st = c.playbackState
        val md = c.metadata
        val pos = st?.let {
            MediaMath.estimatePosition(it.state, it.position, it.lastPositionUpdateTime, SystemClock.elapsedRealtime(), it.playbackSpeed)
        }
        return Snapshot(
            title = md?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.trim(),
            channel = MediaMath.pickChannel(
                md?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                md?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST),
                md?.getString(MediaMetadata.METADATA_KEY_AUTHOR),
            ),
            positionMs = pos,
            playing = st != null && MediaMath.isPlaying(st.state),
            controller = c,
        )
    }

    /** Starts playback unless it is already running (the user may have resumed it by hand). */
    fun resume(): Boolean = try {
        val c = controller()
        if (c != null && !MediaMath.isPlaying(c.playbackState?.state ?: PlaybackState.STATE_NONE)) {
            c.transportControls.play()
            true
        } else {
            false
        }
    } catch (_: Throwable) {
        false
    }
}
