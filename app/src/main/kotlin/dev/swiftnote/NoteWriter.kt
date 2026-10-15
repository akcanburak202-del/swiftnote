package dev.swiftnote

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

/** Appends notes with plain java.io. Never rewrites existing content. Failures are returned, never thrown. */
class NoteWriter(private val dir: File) {
    fun fileFor(title: String?, channel: String?): File = File(
        dir,
        if (title.isNullOrBlank()) NoteFormat.UNTITLED_FILE_NAME else NoteFormat.fileNameFor(title, channel ?: ""),
    )

    /** Appends one note; creates dir and file (with frontmatter) on first write. Returns the file, or the failure. */
    fun append(title: String?, channel: String?, time: String?, text: String, date: String): Result<File> = runCatching {
        if (!dir.isDirectory && !dir.mkdirs() && !dir.isDirectory) throw IOException("Cannot create ${dir.path}")
        val file = fileFor(title, channel)
        val sb = StringBuilder()
        if (!file.exists() || file.length() == 0L) {
            sb.append(NoteFormat.initialContent(title, channel, date))
        } else if (!endsWithNewline(file)) {
            sb.append('\n')
        }
        sb.append(NoteFormat.noteLine(time, text))
        FileOutputStream(file, true).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
            out.fd.sync() // on disk before the caller treats the note as saved
        }
        file
    }

    private fun endsWithNewline(file: File): Boolean = RandomAccessFile(file, "r").use { raf ->
        raf.seek(raf.length() - 1)
        raf.read() == '\n'.code
    }
}
