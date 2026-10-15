package dev.swiftnote.probe.media

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var out: TextView
    private val ytPackage = "com.google.android.youtube"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        fun button(label: String, action: () -> Unit) =
            root.addView(Button(this).apply { text = label; setOnClickListener { action() } })
        button("Bildirim erisimi ayari") { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        button("Yenile / oturumlari listele") { refresh() }
        button("3 sn sonra durdur") { handler.postDelayed({ transport { it.pause() }; refreshLater() }, 3000) }
        button("3 sn sonra devam") { handler.postDelayed({ transport { it.play() }; refreshLater() }, 3000) }
        button("Konumu oku") { refresh(positionOnly = true) }
        button("Yedek: 3 sn sonra MEDIA_PAUSE") { handler.postDelayed({ key(KeyEvent.KEYCODE_MEDIA_PAUSE) }, 3000) }
        button("Yedek: 3 sn sonra MEDIA_PLAY") { handler.postDelayed({ key(KeyEvent.KEYCODE_MEDIA_PLAY) }, 3000) }
        out = TextView(this).apply { textSize = 14f; typeface = Typeface.MONOSPACE; setTextIsSelectable(true) }
        root.addView(ScrollView(this).apply { addView(out) })
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun sessions(): List<MediaController>? = try {
        getSystemService(MediaSessionManager::class.java)
            .getActiveSessions(ComponentName(this, ProbeListener::class.java))
    } catch (e: SecurityException) {
        null
    }

    private fun transport(block: (MediaController.TransportControls) -> Unit) {
        val yt = sessions()?.firstOrNull { it.packageName == ytPackage } ?: return
        block(yt.transportControls)
    }

    private fun key(code: Int) {
        val am = getSystemService(AudioManager::class.java)
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    /** The state settles a moment after pause/play. */
    private fun refreshLater() {
        handler.postDelayed({ refresh(positionOnly = true) }, 500)
    }

    private fun refresh(positionOnly: Boolean = false) {
        val list = sessions()
        if (list == null) {
            out.text = "SecurityException: bildirim erisimi verilmemis (Probe Media session access)."
            return
        }
        val sb = StringBuilder("aktif oturum: ${list.size}\n")
        for (c in list) {
            if (c.packageName != ytPackage && positionOnly) continue
            sb.append("\n== ${c.packageName}\n")
            val st = c.playbackState
            if (c.packageName == ytPackage && st != null) {
                val age = SystemClock.elapsedRealtime() - st.lastPositionUpdateTime
                val est = if (st.state == PlaybackState.STATE_PLAYING) {
                    st.position + (age * st.playbackSpeed).toLong()
                } else {
                    st.position
                }
                sb.append("konum (ham)   : ${TimeFormat.format(st.position)}\n")
                sb.append("konum (tahmin): ${TimeFormat.format(est)}\n")
            }
            if (positionOnly) continue
            val md = c.metadata
            if (md != null) {
                for (k in listOf(
                    MediaMetadata.METADATA_KEY_TITLE, MediaMetadata.METADATA_KEY_ARTIST,
                    MediaMetadata.METADATA_KEY_ALBUM, MediaMetadata.METADATA_KEY_MEDIA_ID,
                )) {
                    sb.append("${k.substringAfterLast('.')}: ${md.getString(k)}\n")
                }
                val d = md.getLong(MediaMetadata.METADATA_KEY_DURATION)
                sb.append("DURATION: $d (${TimeFormat.format(d)})\n")
                sb.append("MEDIA_ID is 11-char id: ${VideoId.isId(md.getString(MediaMetadata.METADATA_KEY_MEDIA_ID))}\n")
            } else {
                sb.append("metadata: null\n")
            }
            if (st != null) {
                sb.append(
                    "state=${st.state} position=${st.position} lastPositionUpdateTime=${st.lastPositionUpdateTime}" +
                        " speed=${st.playbackSpeed} (elapsedRealtime now=${SystemClock.elapsedRealtime()})\n",
                )
            } else {
                sb.append("playbackState: null\n")
            }
        }
        out.text = sb.toString()
    }
}
