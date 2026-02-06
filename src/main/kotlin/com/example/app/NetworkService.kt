package com.example.app

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

object NetworkService {

    private const val TAG = "NetworkService"

    // TODO: Replace with your actual server URL
    const val SERVER_URL = "https://your-server.com/api/upload"

    private val client = OkHttpClient()

    fun sendImageToServer(url: String, imageFile: File) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
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
                Log.e(TAG, "Failed to send image: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "Server returned error: ${it.code}")
                    } else {
                        Log.d(TAG, "Image sent successfully")
                    }
                }
            }
        })
    }
}
