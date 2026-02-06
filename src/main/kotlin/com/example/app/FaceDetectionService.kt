package com.example.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.media.ImageReader
import android.util.Log
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class FaceDetectionService(private val context: Context) {

    companion object {
        private const val TAG = "FaceDetectionService"
    }

    private val detector: FaceDetector = FaceDetector.Builder(context)
        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
        .setMode(FaceDetector.FAST_MODE)
        .build()

    val isOperational: Boolean
        get() = detector.isOperational

    fun detectFaces(image: Image): SparseArray<Face> {
        val bitmap = imageToBitmap(image)
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val faces = detector.detect(frame)
        bitmap.recycle()
        return faces
    }

    fun processImage(reader: ImageReader) {
        val image = reader.acquireLatestImage() ?: return
        try {
            val faces = detectFaces(image)
            if (faces.size() > 0) {
                Log.d(TAG, "Detected ${faces.size()} face(s)")
                val file = imageToFile(image)
                if (file != null) {
                    NetworkService.sendImageToServer(NetworkService.SERVER_URL, file)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image: ${e.message}")
        } finally {
            image.close()
        }
    }

    fun release() {
        detector.release()
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 80, out)
        val jpegBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    private fun imageToFile(image: Image): File? {
        return try {
            val planes = image.planes
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 80, out)

            val file = File(context.cacheDir, "face_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { it.write(out.toByteArray()) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to file: ${e.message}")
            null
        }
    }
}
