package com.observerwatch.domain.notification

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class TelegramSenderTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()

        tempFile = File.createTempFile("test_face", ".jpg")
        tempFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
        if (tempFile.exists()) tempFile.delete()
    }

    @Test
    fun `sendPhoto sends POST request to correct path`() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        val baseUrl = mockServer.url("/").toString()
        val sender = TelegramSender(botToken = "test_token", apiBaseUrl = baseUrl)
        sender.sendPhoto(chatId = "123456", imageFile = tempFile)

        val recorded = mockServer.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.contains("test_token/sendPhoto"))
    }

    @Test
    fun `sendPhoto sends multipart with chat_id and photo`() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        val baseUrl = mockServer.url("/").toString()
        val sender = TelegramSender(botToken = "test_token", apiBaseUrl = baseUrl)
        sender.sendPhoto(chatId = "123456", imageFile = tempFile)

        val recorded = mockServer.takeRequest(5, TimeUnit.SECONDS)!!
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("chat_id"))
        assertTrue(body.contains("123456"))
        assertTrue(body.contains("photo"))
        assertTrue(recorded.getHeader("Content-Type")!!.contains("multipart/form-data"))
    }

    @Test
    fun `sendPhoto does not crash on server error`() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val baseUrl = mockServer.url("/").toString()
        val sender = TelegramSender(botToken = "test_token", apiBaseUrl = baseUrl)
        sender.sendPhoto(chatId = "123456", imageFile = tempFile)

        val recorded = mockServer.takeRequest(5, TimeUnit.SECONDS)
        assertTrue(recorded != null)
    }

    @Test
    fun `sendPhoto deletes file on success`() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        val baseUrl = mockServer.url("/").toString()
        val sender = TelegramSender(botToken = "test_token", apiBaseUrl = baseUrl)
        sender.sendPhoto(chatId = "123456", imageFile = tempFile)

        mockServer.takeRequest(5, TimeUnit.SECONDS)
        // Give async callback time to execute
        Thread.sleep(500)
        assertTrue(!tempFile.exists())
    }
}
