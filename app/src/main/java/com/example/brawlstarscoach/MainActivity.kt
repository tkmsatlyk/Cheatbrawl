
package com.example.brawlstarscoach

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    companion object {
        private const val REQUEST_CAPTURE = 4102
    }

    private lateinit var status: TextView
    private lateinit var motion: TextView

    private var receiverRegistered = false

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            if (intent?.action != ScreenCaptureService.ACTION_STATUS) {
                return
            }

            val message = intent.getStringExtra(
                ScreenCaptureService.EXTRA_MESSAGE
            )

            val score = intent.getFloatExtra(
                ScreenCaptureService.EXTRA_MOTION,
                -1f
            )

            if (message != null) {
                status.text = message
            }

            if (score >= 0f) {
                motion.text = "Motion level: ${
                    (score * 100).toInt()
                }%"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(19, 19, 29))
        }

        val scroll = ScrollView(this)

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "CHEATBRAWL AI"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(180, 110, 255))
            setPadding(0, 20, 0, 10)
        }

        val subtitle = TextView(this).apply {
            text = "REAL-TIME SCREEN ANALYSIS"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }

        status = createLabel("SYSTEM READY")
        motion = createLabel("Motion level: waiting")

        panel.addView(title)
        panel.addView(subtitle)
        panel.addView(status)
        panel.addView(motion)

        addSection(panel, "SCREEN CAPTURE")

        addButton(panel, "START SCREEN ANALYSIS") {
            requestScreenCapture()
        }

        addButton(panel, "STOP SCREEN ANALYSIS") {
            val stopIntent = Intent(
                this,
                ScreenCaptureService::class.java
            ).setAction(ScreenCaptureService.ACTION_STOP)

            startService(stopIntent)
            status.text = "Stopping..."
            motion.text = "Motion level: stopped"
        }

        addSection(panel, "AI ANALYSIS MODULES")

        addInfo(panel, "Frame Analyzer", "Measuring frame changes")
        addInfo(panel, "Enemy Detection", "Model not connected")
        addInfo(panel, "Projectile Tracking", "Model not connected")
        addInfo(panel, "Behavior Learning", "Waiting for observations")
        addInfo(panel, "Dodge Planner", "Waiting for threat coordinates")

        addSection(panel, "SYSTEM INFORMATION")

        addInfo(
            panel,
            "Current engine",
            "Frame-to-frame motion analysis"
        )

        addInfo(
            panel,
            "Next development",
            "Object detection and tracking"
        )

        scroll.addView(panel)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        startActivityForResult(
            manager.createScreenCaptureIntent(),
            REQUEST_CAPTURE
        )
    }

    private fun createLabel(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 18, 0, 18)
        }
    }

    private fun addSection(
        parent: LinearLayout,
        value: String
    ) {
        val label = TextView(this).apply {
            text = value
            textSize = 18f
            setTextColor(Color.rgb(180, 110, 255))
            setPadding(0, 25, 0, 10)
        }

        parent.addView(label)
    }

    private fun addInfo(
        parent: LinearLayout,
        name: String,
        description: String
    ) {
        val label = TextView(this).apply {
            text = "$name\n$description"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
        }

        parent.addView(label)
    }

    private fun addButton(
        parent: LinearLayout,
        value: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = value
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(105, 48, 165))
            setOnClickListener { action() }
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun onResume() {
        super.onResume()

        if (!receiverRegistered) {
            val filter = IntentFilter(
                ScreenCaptureService.ACTION_STATUS
            )

            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(
                    statusReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(statusReceiver, filter)
            }

            receiverRegistered = true
        }
    }

    override fun onPause() {
        if (receiverRegistered) {
            unregisterReceiver(statusReceiver)
            receiverRegistered = false
        }

        super.onPause()
    }

    @Deprecated("Use Activity Result API in production")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CAPTURE) return

        if (resultCode == RESULT_OK && data != null) {

            val serviceIntent = Intent(
                this,
                ScreenCaptureService::class.java
            ).apply {
                putExtra(
                    ScreenCaptureService.EXTRA_RESULT_CODE,
                    resultCode
                )

                putExtra(
                    ScreenCaptureService.EXTRA_DATA,
                    data
                )
            }

            status.text = "Starting capture..."

            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } catch (e: Exception) {
                status.text = "Start error: ${e.message}"
            }

        } else {
            status.text = "Screen capture permission denied"
        }
    }
}
