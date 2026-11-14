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
    private val captureRequest = 4102
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Cheatbrawl AI Coach"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Hazır. Ekran analizi başlatmak için izin ver."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        val button = Button(this).apply {
            text = "Ekran yakalama izni ver"
            setOnClickListener { requestScreenCapture() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(button)
        setContentView(layout)
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(manager.createScreenCaptureIntent(), captureRequest)
    }

    @Deprecated("Kept minimal for the starter prototype")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == captureRequest) {
            status.text = if (resultCode == RESULT_OK && data != null) {
                "Ekran yakalama izni verildi. Sonraki sürümde görüntü analizi eklenecek."
            } else {
                "Ekran yakalama izni verilmedi."
            }
        }
    }
}
