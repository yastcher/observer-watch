package com.observerwatch.domain.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import com.observerwatch.config.AppConfig

class CameraFrameSource(
    private val cameraManager: CameraManager,
    private val backgroundHandler: Handler
) {

    companion object {
        private const val TAG = "CameraFrameSource"
    }

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    val imageReader: ImageReader = ImageReader.newInstance(
        AppConfig.IMAGE_WIDTH, AppConfig.IMAGE_HEIGHT, ImageFormat.YUV_420_888, 2
    )

    fun start(onError: () -> Unit) {
        val cameraId = findFrontCamera()
        if (cameraId == null) {
            Log.e(TAG, "No front-facing camera found")
            onError()
            return
        }

        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession(onError)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    camera.close()
                    cameraDevice = null
                    onError()
                }
            }, backgroundHandler)
        } catch (e: SecurityException) {
            Log.e(TAG, "Camera permission not granted: ${e.message}")
            onError()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
            onError()
        }
    }

    fun stop() {
        captureSession?.close()
        cameraDevice?.close()
        imageReader.close()
    }

    private fun findFrontCamera(): String? {
        return cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_FRONT
        }
    }

    private fun createCaptureSession(onError: () -> Unit) {
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
                        onError()
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating capture session: ${e.message}")
        }
    }
}
