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

    /** Whole file name limit on the target filesystems, in UTF-8 bytes. */
    const val MAX_FILE_NAME_BYTES = 255
    private const val SEPARATOR = " — "
    private const val EXTENSION = ".md"
    private const val MAX_CHANNEL_BYTES = 80

    /** Longest prefix of [s] that fits [maxBytes] of UTF-8, cut on a code-point boundary. */
    fun truncateUtf8(s: String, maxBytes: Int): String {
        var bytes = 0
        var i = 0
        while (i < s.length) {
            val cp = s.codePointAt(i)
            val n = when {
                cp < 0x80 -> 1
                cp < 0x800 -> 2
                cp < 0x10000 -> 3
                else -> 4
            }
            if (bytes + n > maxBytes) break
            bytes += n
            i += Character.charCount(cp)
        }
        return s.substring(0, i)
    }

    private fun utf8Len(s: String) = s.toByteArray(Charsets.UTF_8).size

    /** `<Başlık> — <Kanal>.md`, at most 255 UTF-8 bytes; the title gets the larger share. Empty parts fall back. */
    fun fileNameFor(title: String, channel: String): String {
        val overhead = utf8Len(SEPARATOR) + utf8Len(EXTENSION)
        val budget = MAX_FILE_NAME_BYTES - overhead
        val c = truncateUtf8(sanitizeFileName(channel).ifEmpty { FALLBACK_CHANNEL }, MAX_CHANNEL_BYTES).trim()
        val t = truncateUtf8(sanitizeFileName(title).ifEmpty { FALLBACK_TITLE }, budget - utf8Len(c)).trim()
        return "${t.ifEmpty { FALLBACK_TITLE }}$SEPARATOR$c$EXTENSION"
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
