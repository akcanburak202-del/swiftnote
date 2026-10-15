package dev.swiftnote.probe.media

object TimeFormat {
    /** mm:ss, or h:mm:ss from one hour; negative values clamp to 0. */
    fun format(ms: Long): String {
        val total = maxOf(ms, 0L) / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }
}

object VideoId {
    private val ID = Regex("^[A-Za-z0-9_-]{11}$")
    private val IN_TEXT = Regex("""(?:youtu\.be/|[?&]v=|/shorts/|/embed/|/live/)([A-Za-z0-9_-]{11})""")

    fun isId(s: String?): Boolean = s != null && ID.matches(s)

    /** Extracts an 11-char video id from a shared URL/text, or null. */
    fun fromText(text: String?): String? = text?.let { IN_TEXT.find(it)?.groupValues?.get(1) }
}
