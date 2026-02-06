package com.observerwatch.domain.camera

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import com.observerwatch.config.AppConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageConverter {

    private const val TAG = "ImageConverter"

    fun extractNv21Bytes(image: Image): ByteArray {
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

        return nv21
    }

    fun nv21ToJpegFile(nv21: ByteArray, width: Int, height: Int, cacheDir: File): File? {
        return try {
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val output = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), AppConfig.JPEG_QUALITY, output)

            val file = File(cacheDir, "face_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { it.write(output.toByteArray()) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error converting NV21 to JPEG file: ${e.message}")
            null
        }
    }
}
