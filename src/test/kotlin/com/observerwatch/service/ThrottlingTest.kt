package com.observerwatch.service

import com.observerwatch.config.AppConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThrottlingTest {

    @Test
    fun `should allow first notification`() {
        var lastTimestamp = 0L
        val now = System.currentTimeMillis()
        val shouldSend = now - lastTimestamp >= AppConfig.NOTIFICATION_COOLDOWN_MS
        assertTrue(shouldSend)
    }

    @Test
    fun `should block notification within cooldown period`() {
        val now = System.currentTimeMillis()
        val lastTimestamp = now - 1000L
        val shouldSend = now - lastTimestamp >= AppConfig.NOTIFICATION_COOLDOWN_MS
        assertFalse(shouldSend)
    }

    @Test
    fun `should allow notification after cooldown expires`() {
        val now = System.currentTimeMillis()
        val lastTimestamp = now - AppConfig.NOTIFICATION_COOLDOWN_MS - 1
        val shouldSend = now - lastTimestamp >= AppConfig.NOTIFICATION_COOLDOWN_MS
        assertTrue(shouldSend)
    }

    @Test
    fun `cooldown constant has reasonable value`() {
        assertTrue(AppConfig.NOTIFICATION_COOLDOWN_MS >= 10_000L)
        assertTrue(AppConfig.NOTIFICATION_COOLDOWN_MS <= 300_000L)
    }
}
