package com.observerwatch.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.observerwatch.R
import com.observerwatch.config.AppConfig
import com.observerwatch.domain.camera.CameraFrameSource
import com.observerwatch.domain.camera.ImageConverter
import com.observerwatch.domain.detection.FaceDetector
import com.observerwatch.domain.notification.TelegramSender

class ObserverForegroundService : Service() {

    companion object {
        private const val TAG = "ObserverService"
    }

    private lateinit var cameraFrameSource: CameraFrameSource
    private lateinit var faceDetector: FaceDetector
    private lateinit var telegramSender: TelegramSender
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private var lastNotificationTimestamp = 0L

    override fun onCreate() {
        super.onCreate()

        startBackgroundThread()
        createNotificationChannel()
        startForegroundNotification()

        faceDetector = FaceDetector()
        telegramSender = TelegramSender(AppConfig.getTelegramBotToken(this))

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        cameraFrameSource = CameraFrameSource(cameraManager, backgroundHandler)

        cameraFrameSource.imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val nv21Bytes = ImageConverter.extractNv21Bytes(image)
            val width = image.width
            val height = image.height
            image.close()
            processFrame(nv21Bytes, width, height)
        }, backgroundHandler)

        cameraFrameSource.start(onError = { stopSelf() })
    }

    private fun processFrame(nv21Bytes: ByteArray, width: Int, height: Int) {
        val now = System.currentTimeMillis()
        if (now - lastNotificationTimestamp < AppConfig.NOTIFICATION_COOLDOWN_MS) {
            return
        }

        faceDetector.detectFaces(nv21Bytes, width, height, rotation = 0) { faces ->
            if (faces.isNotEmpty()) {
                Log.d(TAG, "Detected ${faces.size} face(s)")
                val file = ImageConverter.nv21ToJpegFile(nv21Bytes, width, height, cacheDir)
                if (file != null) {
                    lastNotificationTimestamp = System.currentTimeMillis()
                    telegramSender.sendPhoto(AppConfig.getTelegramChatId(this@ObserverForegroundService), file)
                }
            }
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("ObserverBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error stopping background thread: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AppConfig.NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, AppConfig.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_content_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(AppConfig.NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        cameraFrameSource.stop()
        faceDetector.close()
        stopBackgroundThread()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null
}
