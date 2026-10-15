package dev.swiftnote.probe.storage

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File
import java.time.LocalDate

/**
 * Probe C: can a note be appended to MateBridge/Wi-Fi/Video Notları/<title> — <channel>.md and copied to the
 * clipboard? Throwaway UI; the logic lives in NoteFormat / NoteWriter. Never logs note text.
 */
class MainActivity : Activity() {
    private lateinit var permission: TextView
    private lateinit var grantButton: Button
    private lateinit var titleField: EditText
    private lateinit var channelField: EditText
    private lateinit var timeField: EditText
    private lateinit var noteField: EditText
    private lateinit var result: TextView

    private val notesDir = File(Environment.getExternalStorageDirectory(), "MateBridge/Wi-Fi/Video Notları")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }

        permission = TextView(this).apply { textSize = 16f }
        grantButton = Button(this).apply {
            text = "Tüm dosyalara erişim ayarını aç"
            setOnClickListener { openAllFilesSettings() }
        }
        titleField = field("Başlık", "Şimşek: Ğ ü ö ç — test")
        channelField = field("Kanal", "Test Kanalı")
        timeField = field("Zaman (mm:ss)", "00:12")
        noteField = field("Not (çok satırlı olabilir)", "").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }
        val add = Button(this).apply { text = "ekle"; setOnClickListener { addNote() } }
        result = TextView(this).apply { textSize = 14f; setTextIsSelectable(true) }

        listOf(permission, grantButton, titleField, channelField, timeField, noteField, add, result).forEach(root::addView)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        val granted = hasAccess()
        permission.text = "Tüm dosyalara erişim: ${if (granted) "VAR" else "YOK"}\nKlasör: ${notesDir.path}"
        grantButton.isEnabled = !granted
    }

    private fun field(hint: String, initial: String) = EditText(this).apply {
        this.hint = hint
        setText(initial)
        inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun hasAccess() = Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()

    private fun openAllFilesSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")),
            )
        } catch (e: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            } catch (e2: Exception) {
                result.text = "Ayar ekranı açılamadı: ${e2.javaClass.simpleName}. Ayarlar'da 'Tüm dosyalara erişim'i elle ara."
            }
        }
    }

    private fun addNote() {
        val title = titleField.text.toString()
        val channel = channelField.text.toString()
        val time = timeField.text.toString().trim().ifEmpty { "00:00" }
        val text = noteField.text.toString()
        if (text.isBlank()) {
            result.text = "Not boş."
            return
        }
        // Disk first; the field is cleared only after a successful write.
        val written = NoteWriter(notesDir).append(title, channel, time, text, LocalDate.now().toString())
        val file = written.getOrNull()
        if (file == null) {
            val e = written.exceptionOrNull()
            result.text = "YAZMA HATASI: ${e?.javaClass?.simpleName}: ${e?.message}\nMetin korundu."
            return
        }
        val clip = try {
            val cm = getSystemService(ClipboardManager::class.java)
            cm.setPrimaryClip(ClipData.newPlainText("SwiftNote", NoteFormat.clipboardText(time, text)))
            "Pano: yazıldı (${NoteFormat.clipboardText(time, text).length} karakter)"
        } catch (e: Exception) {
            "PANO HATASI: ${e.javaClass.simpleName}: ${e.message}"
        }
        noteField.setText("")
        val content = try { file.readText() } catch (e: Exception) { "(okunamadı: ${e.javaClass.simpleName})" }
        result.text = "Dosya: ${file.path}\n$clip\n\n$content"
    }
}
