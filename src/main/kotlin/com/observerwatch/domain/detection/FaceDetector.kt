package com.observerwatch.domain.detection

import android.graphics.ImageFormat
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetector {

    companion object {
        private const val TAG = "FaceDetector"
    }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()

    private val detector = FaceDetection.getClient(options)

    fun detectFaces(
        nv21Bytes: ByteArray,
        width: Int,
        height: Int,
        rotation: Int,
        onResult: (List<Face>) -> Unit
    ) {
        val inputImage = InputImage.fromByteArray(
            nv21Bytes, width, height, rotation, ImageFormat.NV21
        )
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                onResult(faces)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed: ${e.message}")
                onResult(emptyList())
            }
    }

    fun close() {
        detector.close()
    }
}
