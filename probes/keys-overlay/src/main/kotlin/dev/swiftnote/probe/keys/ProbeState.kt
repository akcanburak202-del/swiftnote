package dev.swiftnote.probe.keys

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

enum class OverlayPath { A, B, C }

/** In-process state shared by the service, the overlay and the main screen. Probe-grade, not thread-hardened. */
object ProbeState {
    const val TAG = "SNKeysProbe"
    const val YOUTUBE = "com.google.android.youtube"

    @Volatile var service: ProbeService? = null
    @Volatile var serviceConnected = false
    @Volatile var filterOn = false
    /** True when the last filter switch failed: the real filter state is not known. */
    @Volatile var filterUnknown = false
    @Volatile var activeBox: NoteBox? = null
    @Volatile var focusedPackage: String? = null
    @Volatile var boxOpen = false
    @Volatile var nonYoutubeKeyEvents = 0

    private val keyLog = ArrayDeque<String>()
    private val transitions = ArrayDeque<String>()
    private val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences("probe", Context.MODE_PRIVATE)

    fun path(c: Context): OverlayPath =
        runCatching { OverlayPath.valueOf(prefs(c).getString("path", "A")!!) }.getOrDefault(OverlayPath.A)

    fun setPath(c: Context, p: OverlayPath) = prefs(c).edit().putString("path", p.name).apply()

    /** Draft text is stored on disk but NEVER logged. */
    fun draft(c: Context): String = prefs(c).getString("draft", "") ?: ""

    /** commit(): synchronous, so the draft is on disk before the box closes. */
    fun setDraft(c: Context, text: String): Boolean = prefs(c).edit().putString("draft", text).commit()

    /** Stand-in for the note file in this probe. Shown on the main screen, NEVER logged. */
    fun lastNote(c: Context): String = prefs(c).getString("lastNote", "") ?: ""

    fun setLastNote(c: Context, text: String): Boolean = prefs(c).edit().putString("lastNote", text).commit()

    /** Only called while YouTube has input focus. In memory only: key codes never go to logcat. */
    @Synchronized fun logYoutubeKey(keyCode: Int, action: Int, meta: Int, repeat: Int) {
        val line = "${fmt.format(Date())} key=$keyCode act=$action meta=0x${meta.toString(16)} rep=$repeat"
        keyLog.addFirst(line)
        while (keyLog.size > 20) keyLog.removeLast()
    }

    @Synchronized fun recentKeys(): List<String> = keyLog.toList()

    @Synchronized fun logTransition(msg: String) {
        val line = "${fmt.format(Date())} $msg"
        transitions.addFirst(line)
        while (transitions.size > 20) transitions.removeLast()
        Log.i(TAG, line)
    }

    @Synchronized fun recentTransitions(): List<String> = transitions.toList()
}
