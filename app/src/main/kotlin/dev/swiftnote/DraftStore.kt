package dev.swiftnote

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * The unsaved draft. SharedPreferences with a synchronous commit(); if that fails, a backup file in filesDir.
 * The backup file is authoritative whenever it exists: an empty file is a durable "cleared" marker that wins over a
 * stale prefs value. The in-memory cache is always updated, even when no disk write worked, so the text still comes
 * back on the next shortcut. [save] and the first [current] touch the disk: call them off the main thread
 * (or at shutdown only); the main thread may use [cached]. The text is never logged.
 */
class DraftStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("draft", Context.MODE_PRIVATE)
    private val backup = File(context.applicationContext.filesDir, "draft.bak")

    @Volatile private var cache: String? = null

    /** The draft if it is already in memory, else null (never reads the disk). */
    fun cached(): String? = cache

    /** The draft, from memory once loaded; the first call reads the disk. */
    fun current(): String {
        cache?.let { return it }
        val text = try {
            if (backup.exists()) backup.readText(Charsets.UTF_8) else prefs.getString(KEY, "") ?: ""
        } catch (_: Throwable) {
            prefs.getString(KEY, "") ?: ""
        }
        // A concurrent save() may have filled the cache meanwhile: it wins.
        if (cache == null) cache = text
        return cache ?: text
    }

    /** Stores [text] (blank clears the draft). True when it is durable on disk; the cache is updated either way. */
    fun save(text: String): Boolean {
        val value = if (text.isBlank()) "" else text
        cache = value
        val ok = try {
            prefs.edit().putString(KEY, value).commit()
        } catch (_: Throwable) {
            false
        }
        if (ok) {
            // The backup would shadow the fresh prefs value. If it cannot be removed, overwrite it with the value.
            if (backup.exists() && !runCatching { backup.delete() }.getOrDefault(false)) return writeBackup(value)
            return true
        }
        return writeBackup(value)
    }

    private fun writeBackup(value: String): Boolean = try {
        FileOutputStream(backup).use { out ->
            out.write(value.toByteArray(Charsets.UTF_8))
            out.fd.sync()
        }
        true
    } catch (_: Throwable) {
        false
    }

    private companion object {
        const val KEY = "draft"
    }
}
