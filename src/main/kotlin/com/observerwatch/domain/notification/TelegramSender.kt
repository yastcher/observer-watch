package com.observerwatch.domain.notification

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

class TelegramSender(
    private val botToken: String,
    private val apiBaseUrl: String = TELEGRAM_API_BASE
) {

    companion object {
        private const val TAG = "TelegramSender"
        private const val TELEGRAM_API_BASE = "https://api.telegram.org/bot"
    }

    private val client = OkHttpClient()

    fun sendPhoto(chatId: String, imageFile: File) {
        val url = "${apiBaseUrl}${botToken}/sendPhoto"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart(
                "photo",
                imageFile.name,
                imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send photo to Telegram: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "Telegram API error: ${it.code}")
                    } else {
                        Log.d(TAG, "Photo sent to Telegram successfully")
                        imageFile.delete()
                    }
                }
            }
        })
    }
}
