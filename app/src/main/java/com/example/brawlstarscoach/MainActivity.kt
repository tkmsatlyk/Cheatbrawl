
package com.example.brawlstarscoach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val requestCode = 4102
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "CHEATBRAWL AI COACH"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "System ready"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 30)
        }

        val startButton = Button(this).apply {
            text = "START SCREEN ANALYSIS"
            setOnClickListener {
                val manager = getSystemService(
                    Context.MEDIA_PROJECTION_SERVICE
                ) as MediaProjectionManager

                startActivityForResult(
                    manager.createScreenCaptureIntent(),
                    requestCode
                )
            }
        }

        val stopButton = Button(this).apply {
            text = "STOP SCREEN ANALYSIS"
            setOnClickListener {
                startService(
                    Intent(this@MainActivity, ScreenCaptureService::class.java)
                        .setAction(ScreenCaptureService.ACTION_STOP)
                )
                status.text = "Screen capture stopped"
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    @Deprecated("Use Activity Result API in production")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.requestCode) {
            if (resultCode == RESULT_OK && data != null) {
                val serviceIntent = Intent(
                    this,
                    ScreenCaptureService::class.java
                ).apply {
                    putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
                    putExtra(ScreenCaptureService.EXTRA_DATA, data)
                }

                status.text = "Starting screen capture..."

                startForegroundService(serviceIntent)
            } else {
                status.text = "Screen permission denied"
            }
        }
    }
}
