package com.example.app

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class NetworkServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        tempFile = File.createTempFile("test_image", ".jpg")
        tempFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
    }

    @After
    fun tearDown() {
        server.shutdown()
        tempFile.delete()
    }

    @Test
    fun `sendImageToServer sends multipart POST request`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val url = server.url("/api/upload").toString()
        NetworkService.sendImageToServer(url, tempFile)

        // Wait for async request
        val request = server.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS)

        assertTrue("Request should not be null", request != null)
        assertEquals("POST", request!!.method)
        assertTrue(
            "Content-Type should be multipart",
            request.getHeader("Content-Type")?.contains("multipart/form-data") == true
        )
    }

    @Test
    fun `sendImageToServer includes image in request body`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val url = server.url("/api/upload").toString()
        NetworkService.sendImageToServer(url, tempFile)

        val request = server.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS)

        assertTrue("Request should not be null", request != null)
        val body = request!!.body.readUtf8()
        assertTrue("Body should contain filename", body.contains(tempFile.name))
        assertTrue("Body should contain form field 'image'", body.contains("name=\"image\""))
    }

    @Test
    fun `sendImageToServer handles server error without crashing`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val url = server.url("/api/upload").toString()
        // Should not throw
        NetworkService.sendImageToServer(url, tempFile)

        val request = server.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS)
        assertTrue("Request should still be sent", request != null)
    }

    @Test
    fun `SERVER_URL constant is defined`() {
        assertTrue(
            "SERVER_URL should not be empty",
            NetworkService.SERVER_URL.isNotEmpty()
        )
    }
}
