package com.example.app

import android.content.Context
import com.google.android.gms.vision.face.FaceDetector
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.mock

class FaceDetectionServiceTest {

    @Test
    fun `service creates face detector`() {
        val context = mock<Context>()
        val service = FaceDetectionService(context)
        assertNotNull("FaceDetectionService should be created", service)
    }

    @Test
    fun `release does not throw`() {
        val context = mock<Context>()
        val service = FaceDetectionService(context)
        // Should not throw
        service.release()
    }
}
