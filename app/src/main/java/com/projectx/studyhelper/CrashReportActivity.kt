package com.projectx.studyhelper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Diagnostic crash screen that runs in an isolated process (:crash).
 * Uses pure Android framework views (no Compose/Room/DataStore dependencies)
 * so it is 100% crash-proof and guaranteed to render.
 */
class CrashReportActivity : Activity() {

    companion object {
        const val EXTRA_CRASH_INFO = "extra_crash_info"

        fun createIntent(context: Context, crashInfo: String): Intent {
            return Intent(context, CrashReportActivity::class.java).apply {
                putExtra(EXTRA_CRASH_INFO, crashInfo)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashInfo = intent.getStringExtra(EXTRA_CRASH_INFO) ?: "No crash details available."

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(48, 64, 48, 48)
        }

        // Header Title
        val titleView = TextView(this).apply {
            text = "Project X — Diagnostic Report"
            setTextColor(Color.parseColor("#FF6F00"))
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 16)
        }
        rootLayout.addView(titleView)

        // Subtitle
        val subtitleView = TextView(this).apply {
            text = "An unexpected error occurred. Copy this report to resolve the issue:"
            setTextColor(Color.parseColor("#EEEEEE"))
            textSize = 14f
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(subtitleView)

        // Scrollable Crash Log View
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(24, 24, 24, 24)
        }

        val logView = TextView(this).apply {
            text = crashInfo
            setTextColor(Color.parseColor("#81C784"))
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        scrollView.addView(logView)
        rootLayout.addView(scrollView)

        // Button Bar
        val buttonBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        val copyButton = Button(this).apply {
            text = "Copy Stack Trace"
            setBackgroundColor(Color.parseColor("#6650A4"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Project X Crash Log", crashInfo)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@CrashReportActivity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
        buttonBar.addView(copyButton)

        val spacer = TextView(this).apply {
            width = 32
        }
        buttonBar.addView(spacer)

        val restartButton = Button(this).apply {
            text = "Restart App"
            setBackgroundColor(Color.parseColor("#37474F"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                finish()
                Runtime.getRuntime().exit(0)
            }
        }
        buttonBar.addView(restartButton)

        rootLayout.addView(buttonBar)

        setContentView(rootLayout)
    }
}
