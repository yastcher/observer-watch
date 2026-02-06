package com.example.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class CameraService : Service() {

    companion object {
        private const val TAG = "CameraService"
        private const val CHANNEL_ID = "CameraServiceChannel"
        private const val NOTIFICATION_ID = 1
        private const val IMAGE_WIDTH = 640
        private const val IMAGE_HEIGHT = 480
    }

    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var imageReader: ImageReader
    private lateinit var faceDetectionService: FaceDetectionService
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    override fun onCreate() {
        super.onCreate()

        startBackgroundThread()

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        faceDetectionService = FaceDetectionService(this)

        if (!faceDetectionService.isOperational) {
            Log.e(TAG, "Face detector is not operational")
            stopSelf()
            return
        }

        imageReader = ImageReader.newInstance(
            IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.YUV_420_888, 2
        )
        imageReader.setOnImageAvailableListener({ reader ->
            faceDetectionService.processImage(reader)
        }, backgroundHandler)

        createNotificationChannel()
        startForegroundNotification()
        openCamera()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Observer Watch Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Face monitoring service notification"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Observer Watch")
            .setContentText("Monitoring for faces...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun openCamera() {
        try {
            val cameraId = findFrontCamera()
            if (cameraId == null) {
                Log.e(TAG, "No front-facing camera found")
                stopSelf()
                return
            }

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    camera.close()
                    cameraDevice = null
                    stopSelf()
                }
            }, backgroundHandler)
        } catch (e: SecurityException) {
            Log.e(TAG, "Camera permission not granted: ${e.message}")
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
            stopSelf()
        }
    }

    private fun findFrontCamera(): String? {
        return cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_FRONT
        }
    }

    private fun createCaptureSession() {
        val camera = cameraDevice ?: return
        try {
            val surface = imageReader.surface
            val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequest.addTarget(surface)

            camera.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                captureRequest.build(), null, backgroundHandler
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error starting capture: ${e.message}")
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Capture session configuration failed")
                        stopSelf()
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating capture session: ${e.message}")
        }
    }

    override fun onDestroy() {
        captureSession?.close()
        cameraDevice?.close()
        imageReader.close()
        faceDetectionService.release()
        stopBackgroundThread()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null
}
