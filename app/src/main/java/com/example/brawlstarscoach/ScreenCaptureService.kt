
package com.example.brawlstarscoach

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_STOP = "STOP_CAPTURE"
        const val ACTION_STATUS = "CHEATBRAWL_STATUS"

        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "result_data"
        const val EXTRA_MOTION = "motion_score"
        const val EXTRA_MESSAGE = "message"

        private const val CHANNEL_ID = "capture_channel"
        private const val NOTIFICATION_ID = 4102
    }

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private val analyzer = FrameAnalyzer()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACTION_STOP) {
            stopCapture()
            stopSelf()
            return START_NOT_STICKY
        }

        if (projection != null) return START_NOT_STICKY

        createNotificationChannel()

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("CheatBrawl AI Coach")
            .setContentText("Screen analysis is active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            val resultCode = intent?.getIntExtra(
                EXTRA_RESULT_CODE,
                Activity.RESULT_CANCELED
            ) ?: Activity.RESULT_CANCELED

            val data: Intent? = if (Build.VERSION.SDK_INT >= 33) {
                intent?.getParcelableExtra(
                    EXTRA_DATA,
                    Intent::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra(EXTRA_DATA)
            }

            if (resultCode != Activity.RESULT_OK || data == null) {
                sendStatus("Screen permission missing")
                stopSelf()
                return START_NOT_STICKY
            }

            val manager = getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

            projection = manager.getMediaProjection(
                resultCode,
                data
            )

            startCapture()

        } catch (e: Exception) {
            sendStatus("Capture error: ${e.message}")
            stopCapture()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startCapture() {

        val metrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        val windowManager = getSystemService(
            Context.WINDOW_SERVICE
        ) as WindowManager

        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        handlerThread = HandlerThread(
            "CheatBrawlAnalyzer"
        ).apply {
            start()
        }

        handler = Handler(handlerThread!!.looper)

        imageReader = ImageReader.newInstance(
            width,
            height,
            PixelFormat.RGBA_8888,
            2
        )

        imageReader?.setOnImageAvailableListener(
            { reader ->

                val image = reader.acquireLatestImage()
                    ?: return@setOnImageAvailableListener

                try {
                    val result = analyzer.analyze(image)

                    sendMotion(result.motionScore)

                } catch (e: Exception) {
                    sendStatus("Frame error: ${e.message}")
                } finally {
                    image.close()
                }

            },
            handler
        )

        projection?.registerCallback(
            object : MediaProjection.Callback() {
                override fun onStop() {
                    stopCapture()
                    stopSelf()
                }
            },
            handler
        )

        virtualDisplay = projection?.createVirtualDisplay(
            "CheatBrawlScreen",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )

        if (virtualDisplay == null) {
            throw IllegalStateException(
                "VirtualDisplay creation failed"
            )
        }

        sendStatus("Screen capture active")
    }

    private fun sendMotion(score: Float) {
        val intent = Intent(ACTION_STATUS).apply {
            setPackage(packageName)
            putExtra(EXTRA_MOTION, score)
            putExtra(EXTRA_MESSAGE, "Analyzing frames")
        }
        sendBroadcast(intent)
    }

    private fun sendStatus(message: String) {
        val intent = Intent(ACTION_STATUS).apply {
            setPackage(packageName)
            putExtra(EXTRA_MESSAGE, message)
        }
        sendBroadcast(intent)
    }

    private fun stopCapture() {

        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.setOnImageAvailableListener(null, null)
        imageReader?.close()
        imageReader = null

        val oldProjection = projection
        projection = null
        oldProjection?.stop()

        handlerThread?.quitSafely()
        handlerThread = null
        handler = null

        analyzer.reset()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen analysis",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }
}
