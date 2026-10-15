package dev.swiftnote.probe.media

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

class ShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = if (intent?.action == Intent.ACTION_SEND) intent.getStringExtra(Intent.EXTRA_TEXT) else null
        val subject = intent?.getStringExtra(Intent.EXTRA_SUBJECT)
        val tv = TextView(this).apply {
            textSize = 18f
            setPadding(48, 48, 48, 48)
            setTextIsSelectable(true)
            this.text = "EXTRA_TEXT:\n$text\n\nEXTRA_SUBJECT: $subject\n\nvideo id: ${VideoId.fromText(text)}"
        }
        setContentView(ScrollView(this).apply { addView(tv) })
    }
}
