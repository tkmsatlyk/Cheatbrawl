
package com.example.brawlstarscoach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.graphics.Typeface

class MainActivity : Activity() {

    private val requestCode = 4102
    private lateinit var status: TextView
    private lateinit var prefs: android.content.SharedPreferences

    private val purple = Color.rgb(170, 90, 255)
    private val dark = Color.rgb(20, 20, 29)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("cheatbrawl", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dark)
            setPadding(20, 20, 20, 20)
        }

        val scroll = ScrollView(this)
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "CHEATBRAWL"
            textSize = 30f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "AI COACH  |  CONTROL PANEL"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "SYSTEM READY"
            textSize = 16f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 25)
        }

        menu.addView(title)
        menu.addView(subtitle)
        menu.addView(status)

        addSection(menu, "SCREEN CONTROL")

        addButton(menu, "START SCREEN ANALYSIS") {
            val manager = getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

            startActivityForResult(
                manager.createScreenCaptureIntent(),
                requestCode
            )
        }

        addButton(menu, "STOP SCREEN ANALYSIS") {
            startService(
                Intent(this, ScreenCaptureService::class.java)
                    .setAction(ScreenCaptureService.ACTION_STOP)
            )
            status.text = "SCREEN ANALYSIS STOPPED"
        }

        addSection(menu, "AI FEATURES")

        addToggle(menu, "Enemy Detection", true)
        addToggle(menu, "Hitbox Visualization", true)
        addToggle(menu, "Bullet Tracking", true)
        addToggle(menu, "Projectile Prediction", true)
        addToggle(menu, "Danger Radar", true)
        addToggle(menu, "AI Enemy Learning", true)

        addSection(menu, "AUTO DODGE SETTINGS")

        addToggle(menu, "Auto Dodge Analysis", true)

        addSlider(menu, "Detection Sensitivity", 80)
        addSlider(menu, "Prediction Strength", 85)
        addSlider(menu, "Danger Distance", 70)
        addSlider(menu, "Reaction Priority", 90)

        addSection(menu, "AI INFORMATION")

        val info = TextView(this).apply {
            text = """
                Enemy Detection: READY
                Hitbox Engine: WAITING
                Projectile Engine: WAITING
                AI Prediction: WAITING
                Dodge Analysis: WAITING

                Analysis engines must be connected
                to the screen capture service.
            """.trimIndent()
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(10, 15, 10, 20)
        }

        menu.addView(info)

        scroll.addView(menu)
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

    private fun addSection(
        parent: LinearLayout,
        name: String
    ) {
        val text = TextView(this).apply {
            this.text = name
            textSize = 18f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 25, 0, 12)
        }
        parent.addView(text)
    }

    private fun addToggle(
        parent: LinearLayout,
        name: String,
        default: Boolean
    ) {
        val toggle = Switch(this).apply {
            text = name
            textSize = 15f
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean(name, default)

            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(name, checked).apply()
            }
        }

        parent.addView(toggle)
    }

    private fun addSlider(
        parent: LinearLayout,
        name: String,
        default: Int
    ) {
        val saved = prefs.getInt(name, default)

        val label = TextView(this).apply {
            text = "$name: $saved%"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 0)
        }

        val slider = SeekBar(this).apply {
            max = 100
            progress = saved

            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        label.text = "$name: $progress%"
                        if (fromUser) {
                            prefs.edit()
                                .putInt(name, progress)
                                .apply()
                        }
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {}

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {}
                }
            )
        }

        parent.addView(label)
        parent.addView(slider)
    }

    private fun addButton(
        parent: LinearLayout,
        name: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = name
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(100, 45, 160))
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
                    putExtra(
                        ScreenCaptureService.EXTRA_RESULT_CODE,
                        resultCode
                    )
                    putExtra(
                        ScreenCaptureService.EXTRA_DATA,
                        data
                    )
                }

                status.text = "STARTING SCREEN CAPTURE..."

                startForegroundService(serviceIntent)

            } else {
                status.text = "PERMISSION DENIED"
            }
        }
    }
}
