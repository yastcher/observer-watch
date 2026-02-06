package com.observerwatch.config

import android.content.Context
import android.content.SharedPreferences

object AppConfig {

    private const val PREFS_NAME = "ObserverWatchPrefs"
    private const val KEY_BOT_TOKEN = "telegramBotToken"
    private const val KEY_CHAT_ID = "telegramChatId"

    const val NOTIFICATION_COOLDOWN_MS = 30_000L

    const val IMAGE_WIDTH = 640
    const val IMAGE_HEIGHT = 480
    const val JPEG_QUALITY = 80

    const val NOTIFICATION_CHANNEL_ID = "ObserverWatchChannel"
    const val NOTIFICATION_ID = 1

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTelegramBotToken(context: Context): String {
        return prefs(context).getString(KEY_BOT_TOKEN, "") ?: ""
    }

    fun getTelegramChatId(context: Context): String {
        return prefs(context).getString(KEY_CHAT_ID, "") ?: ""
    }

    fun saveTelegramCredentials(context: Context, botToken: String, chatId: String) {
        with(prefs(context).edit()) {
            putString(KEY_BOT_TOKEN, botToken)
            putString(KEY_CHAT_ID, chatId)
            apply()
        }
    }

    fun hasCredentials(context: Context): Boolean {
        return getTelegramBotToken(context).isNotBlank() &&
            getTelegramChatId(context).isNotBlank()
    }
}
