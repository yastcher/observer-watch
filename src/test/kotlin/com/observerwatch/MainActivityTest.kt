package com.observerwatch

import com.observerwatch.service.ObserverForegroundService
import org.junit.Assert.assertNotNull
import org.junit.Test

class MainActivityTest {

    @Test
    fun `MainActivity class exists`() {
        assertNotNull(MainActivity::class.java)
    }

    @Test
    fun `ObserverForegroundService class exists`() {
        assertNotNull(ObserverForegroundService::class.java)
    }
}
