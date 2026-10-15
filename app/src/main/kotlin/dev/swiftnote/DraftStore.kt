package dev.swiftnote

import android.content.Context
import java.io.File

/**
 * The unsaved draft. SharedPreferences with a synchronous commit(); if that fails, a backup file in filesDir.
 * [save] and the first [current] touch the disk: call them off the main thread (or at shutdown only).
 * The text is never logged.
 */
class DraftStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("draft", Context.MODE_PRIVATE)
    private val backup = File(context.applicationContext.filesDir, "draft.bak")

    @Volatile private var cache: String? = null

    /** The draft, from memory once loaded. Prefs first, then the backup file. */
    fun current(): String {
        cache?.let { return it }
        // The backup exists only when the last prefs commit failed, so it is the newer one.
        val text = readBackup().ifEmpty { prefs.getString(KEY, "") ?: "" }
        cache = text
        return text
    }

    /** Stores [text] (blank clears the draft). True when it is on disk somewhere. */
    fun save(text: String): Boolean {
        val value = if (text.isBlank()) "" else text
        val ok = try {
            prefs.edit().putString(KEY, value).commit()
        } catch (_: Throwable) {
            false
        }
        val stored = if (ok) {
            runCatching { backup.delete() }
            true
        } else {
            writeBackup(value)
        }
        if (stored) cache = value
        return stored
    }

    private fun readBackup(): String = try {
        if (backup.isFile) backup.readText(Charsets.UTF_8) else ""
    } catch (_: Throwable) {
        ""
    }

    private fun writeBackup(value: String): Boolean = try {
        if (value.isEmpty()) {
            !backup.exists() || backup.delete()
        } else {
            java.io.FileOutputStream(backup).use { out ->
                out.write(value.toByteArray(Charsets.UTF_8))
                out.fd.sync()
            }
            true
        }
    } catch (_: Throwable) {
        false
    }

    private companion object {
        const val KEY = "draft"
    }
}
