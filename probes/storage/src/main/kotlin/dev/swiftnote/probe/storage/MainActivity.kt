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
import java.util.concurrent.Executors

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
    private lateinit var addButton: Button
    private val io = Executors.newSingleThreadExecutor()

    private val notesDir = File(Environment.getExternalStorageDirectory(), "MateBridge/Wi-Fi/Video Notları")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }

        permission = TextView(this).apply { textSize = 16f }
        grantButton = Button(this).apply {
            text = "Tüm dosyalara erişim ayarını aç"
            setOnClickListener { openAllFilesSettings() }
        }
        titleField = field(1001, "Başlık", "Şimşek: Ğ ü ö ç — test")
        channelField = field(1002, "Kanal", "Test Kanalı")
        timeField = field(1003, "Zaman (mm:ss)", "00:12")
        noteField = field(1004, "Not (çok satırlı olabilir)", "").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }
        addButton = Button(this).apply { text = "ekle"; setOnClickListener { addNote() } }
        result = TextView(this).apply { id = 1005; freezesText = true; textSize = 14f; setTextIsSelectable(true) }

        listOf(permission, grantButton, titleField, channelField, timeField, noteField, addButton, result).forEach(root::addView)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        val granted = hasAccess()
        permission.text = "Tüm dosyalara erişim: ${if (granted) "VAR" else "YOK"}\nKlasör: ${notesDir.path}"
        grantButton.isEnabled = !granted
    }

    private fun field(viewId: Int, hint: String, initial: String) = EditText(this).apply {
        id = viewId // stable id so the framework restores the text after recreation
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
        addButton.isEnabled = false
        result.text = "Yazılıyor..."
        val date = LocalDate.now().toString()
        // Disk work off the UI thread; order stays write -> clipboard -> show.
        io.execute {
            val written = NoteWriter(notesDir).append(title, channel, time, text, date)
            val file = written.getOrNull()
            val content = file?.let { f -> try { f.readText() } catch (e: Exception) { "(okunamadı: ${e.javaClass.simpleName})" } }
            runOnUiThread {
                addButton.isEnabled = true
                if (file == null) {
                    val e = written.exceptionOrNull()
                    result.text = "YAZMA HATASI: ${e?.javaClass?.simpleName}: ${e?.message}\nMetin korundu."
                    return@runOnUiThread
                }
                val clipText = NoteFormat.clipboardText(time, text)
                val clip = try {
                    getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("SwiftNote", clipText))
                    "Pano: yazıldı (${clipText.length} karakter)"
                } catch (e: Exception) {
                    "PANO HATASI: ${e.javaClass.simpleName}: ${e.message}"
                }
                if (noteField.text.toString() == text) noteField.setText("") // keep anything typed meanwhile
                result.text = "Dosya: ${file.path}\n$clip\n\n$content"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdown() // a running write still finishes
    }
}
