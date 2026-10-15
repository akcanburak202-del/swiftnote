package dev.swiftnote.probe.storage

/** Pure note-file formatting (docs/SPEC.md "Dosya biçimi"). No Android types. */
object NoteFormat {
    /** Characters removed from file names (SPEC), plus path/control characters. */
    private val forbidden = "/\\:*?\"<>|#^[]".toSet()
    const val MAX_PART_CODE_POINTS = 80
    const val FALLBACK_TITLE = "Başlıksız"
    const val FALLBACK_CHANNEL = "Kanalsız"

    /** Replaces forbidden characters with spaces, collapses whitespace, trims dots/spaces, caps length. Turkish is kept. */
    fun sanitizeFileName(raw: String): String {
        val sb = StringBuilder()
        raw.forEach { c -> sb.append(if (c in forbidden || c.isISOControl()) ' ' else c) }
        val collapsed = sb.toString().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
        val capped = if (collapsed.codePointCount(0, collapsed.length) > MAX_PART_CODE_POINTS) {
            collapsed.substring(0, collapsed.offsetByCodePoints(0, MAX_PART_CODE_POINTS))
        } else collapsed
        return capped.trim().trim('.').trim()
    }

    /** `<Başlık> — <Kanal>.md`; empty parts fall back to placeholders. */
    fun fileNameFor(title: String, channel: String): String {
        val t = sanitizeFileName(title).ifEmpty { FALLBACK_TITLE }
        val c = sanitizeFileName(channel).ifEmpty { FALLBACK_CHANNEL }
        return "$t — $c.md"
    }

    /** `mm:ss`, or `h:mm:ss` from one hour up. Negative clamps to 0. */
    fun formatTime(positionMs: Long): String {
        val total = (if (positionMs < 0) 0 else positionMs) / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    /** `- **mm:ss** text\n`; further lines are indented two spaces. CRLF normalised, trailing blank lines dropped. */
    fun noteLine(time: String, text: String): String {
        val lines = text.replace("\r\n", "\n").replace('\r', '\n').trimEnd().split('\n')
        val sb = StringBuilder("- **").append(time).append("** ").append(lines.first().trimEnd())
        lines.drop(1).forEach { sb.append("\n  ").append(it.trimEnd()) }
        return sb.append('\n').toString()
    }

    /** Frontmatter + title, written only when the file is first created. */
    fun initialContent(title: String, channel: String, date: String): String {
        val t = title.replace('\n', ' ').trim().ifEmpty { FALLBACK_TITLE }
        val c = channel.replace('\n', ' ').trim().ifEmpty { FALLBACK_CHANNEL }
        return "---\nkaynak: youtube\nkanal: ${yamlScalar(c)}\nolusturma: $date\n---\n# $t\n\n"
    }

    /** Quotes a YAML value only when a plain scalar could be misparsed. */
    fun yamlScalar(v: String): String {
        val risky = v.any { it in ":#\"'[]{}&*!|>%@`," } || v != v.trim() || v.startsWith("-") || v.startsWith("?")
        return if (risky) "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" else v
    }

    /** What goes to the clipboard: `mm:ss — note`. */
    fun clipboardText(time: String, text: String): String = "$time — ${text.trim()}"
}
