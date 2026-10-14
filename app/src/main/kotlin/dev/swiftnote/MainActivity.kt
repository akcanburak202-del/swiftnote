package dev.swiftnote

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

/** Empty start screen: shows the build identity so the orchestrator can see which build is installed. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this).apply {
            text = BuildInfo.describe(BuildConfig.GIT_SHA, BuildConfig.BUILD_TIME_UTC)
            textSize = 24f
            gravity = Gravity.CENTER
        }
        setContentView(view)
    }
}
