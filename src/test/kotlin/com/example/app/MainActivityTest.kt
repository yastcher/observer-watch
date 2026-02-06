package com.example.app

import org.junit.Assert.assertNotNull
import org.junit.Test

class MainActivityTest {

    @Test
    fun `activity class exists and can be referenced`() {
        val activityClass = MainActivity::class.java
        assertNotNull("MainActivity class should exist", activityClass)
    }

    @Test
    fun `camera service class exists and can be referenced`() {
        val serviceClass = CameraService::class.java
        assertNotNull("CameraService class should exist", serviceClass)
    }
}
