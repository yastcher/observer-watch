package com.observerwatch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.observerwatch.config.AppConfig
import com.observerwatch.service.ObserverForegroundService

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1001
    }

    private val requiredPermissions = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppConfig.hasCredentials(this)) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopService(Intent(this, ObserverForegroundService::class.java))
            finish()
        }

        if (allPermissionsGranted()) {
            startObserverService()
        } else {
            ActivityCompat.requestPermissions(
                this, requiredPermissions, PERMISSIONS_REQUEST_CODE
            )
        }
    }

    internal fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startObserverService() {
        val statusText = findViewById<TextView>(R.id.statusText)
        statusText.setText(R.string.status_running)

        val intent = Intent(this, ObserverForegroundService::class.java)
        startForegroundService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startObserverService()
            } else {
                val statusText = findViewById<TextView>(R.id.statusText)
                statusText.setText(R.string.status_no_permission)
            }
        }
    }
}
