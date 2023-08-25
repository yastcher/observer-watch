```kotlin
package com.example.app

import android.content.Context
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.Frame
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import java.nio.ByteBuffer

class FaceDetectionService(context: Context) {

    private val detector: FaceDetector

    init {
        val detectorOptions = FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.FAST_MODE)
            .build()

        this.detector = detectorOptions
    }

    fun getFaceDetector(): FaceDetector {
        return detector
    }

    fun detectFaces(image: Image): List<Face> {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val frame = Frame.Builder().setBitmap(bitmap).build()

        return detector.detect(frame)
    }

    fun processImage(reader: ImageReader) {
        val image = reader.acquireLatestImage()
        val faces = detectFaces(image)

        if (faces.isNotEmpty()) {
            // Send the image to the specified URL
            NetworkService.sendImage(image)
        }

        image.close()
    }
}
```
