package dev.swiftnote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Setup screen: build identity, the three permissions with a settings button each, a short how-to, last file name. */
class MainActivity : Activity() {
    private lateinit var a11y: TextView
    private lateinit var notif: TextView
    private lateinit var files: TextView
    private lateinit var last: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (24 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
        }
        fun text(size: Float) = TextView(this).apply { textSize = size }.also { root.addView(it) }
        text(18f).text = BuildInfo.describe(BuildConfig.GIT_SHA, BuildConfig.BUILD_TIME_UTC)
        a11y = text(16f)
        button(root, "Erişilebilirlik ayarları") { open(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
        notif = text(16f)
        button(root, "Bildirim erişimi ayarları") { open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        files = text(16f)
        button(root, "Tüm dosyalara erişim ayarı") { openFilesAccess() }
        text(16f).text = "\nKullanım: YouTube'da Ctrl+Shift+N. Enter kaydeder ve devam ettirir, Shift+Enter yeni satır, " +
            "Esc taslak olarak kapatır."
        last = text(16f)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun button(root: LinearLayout, label: String, action: () -> Unit) {
        root.addView(Button(this).apply { text = label; setOnClickListener { action() } })
    }

    private fun open(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: Throwable) {
        }
    }

    private fun openFilesAccess() {
        try {
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")))
        } catch (_: Throwable) {
            open(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
    }

    private fun secure(name: String): String? = try {
        Settings.Secure.getString(contentResolver, name)
    } catch (_: Throwable) {
        null
    }

    private fun mark(ok: Boolean) = if (ok) "AÇIK" else "KAPALI"

    private fun refresh() {
        val a11yOn = ComponentList.contains(
            secure(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), packageName, SwiftNoteService::class.java.name,
        )
        val notifOn = ComponentList.contains(
            secure("enabled_notification_listeners"), packageName, MediaAccessListener::class.java.name,
        )
        a11y.text = "\nErişilebilirlik servisi: ${mark(a11yOn)}" + if (a11yOn && !SwiftNoteService.connected) " (bağlı değil)" else ""
        notif.text = "\nBildirim erişimi (video başlığı/konum): ${mark(notifOn)}"
        files.text = "\nTüm dosyalara erişim (not dosyası): ${mark(Environment.isExternalStorageManager())}"
        val name = getSharedPreferences("app", Context.MODE_PRIVATE).getString(SwiftNoteService.KEY_LAST_FILE, null)
        last.visibility = View.VISIBLE
        last.text = "\nSon kaydedilen dosya: ${name ?: "yok"}"
    }
}
