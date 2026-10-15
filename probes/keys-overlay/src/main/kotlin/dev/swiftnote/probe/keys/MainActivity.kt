package dev.swiftnote.probe.keys

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

/** Probe status screen. Refreshes twice a second while visible. */
class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (16 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
        }
        statusView = TextView(this).apply { typeface = android.graphics.Typeface.MONOSPACE }
        col.addView(statusView)

        val group = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val labels = mapOf(
            OverlayPath.A to "A accessibility overlay",
            OverlayPath.B to "B application overlay",
            OverlayPath.C to "C transparent activity",
        )
        val current = ProbeState.path(this)
        for ((path, label) in labels) {
            group.addView(
                RadioButton(this).apply {
                    text = label
                    id = android.view.View.generateViewId()
                    isChecked = path == current
                    setOnClickListener { ProbeState.setPath(this@MainActivity, path) }
                },
            )
        }
        col.addView(group)

        col.addView(button("Accessibility settings") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        col.addView(
            button("Overlay permission (path B)") {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                )
            },
        )
        col.addView(button("Turn service off") { ProbeState.service?.disableSelf() })
        setContentView(ScrollView(this).apply { addView(col) })
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        handler.removeCallbacks(tick)
        super.onPause()
    }

    private fun refresh() {
        val s = ProbeState
        val sb = StringBuilder()
        sb.append("service connected: ${s.serviceConnected}\n")
        sb.append("key filter:        ${if (s.filterOn) "ON" else "off"}\n")
        sb.append("focused package:   ${s.focusedPackage ?: "none"}\n")
        sb.append("path:              ${s.path(this)}\n")
        sb.append("box open:          ${s.boxOpen}\n")
        sb.append("overlay permission: ${Settings.canDrawOverlays(this)}\n")
        sb.append("key events outside YouTube (count only): ${s.nonYoutubeKeyEvents}\n")
        sb.append("\nLast YouTube key events (act 0=down 1=up):\n")
        s.recentKeys().forEach { sb.append(it).append('\n') }
        sb.append("\nLast transitions:\n")
        s.recentTransitions().forEach { sb.append(it).append('\n') }
        statusView.text = sb
    }
}
