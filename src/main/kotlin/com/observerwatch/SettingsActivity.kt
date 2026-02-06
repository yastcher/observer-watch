package com.observerwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.observerwatch.config.AppConfig

class SettingsActivity : AppCompatActivity() {

    private lateinit var botTokenEditText: EditText
    private lateinit var chatIdEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        botTokenEditText = findViewById(R.id.botTokenEditText)
        chatIdEditText = findViewById(R.id.chatIdEditText)
        saveButton = findViewById(R.id.saveButton)

        botTokenEditText.setText(AppConfig.getTelegramBotToken(this))
        chatIdEditText.setText(AppConfig.getTelegramChatId(this))

        saveButton.setOnClickListener {
            val botToken = botTokenEditText.text.toString().trim()
            val chatId = chatIdEditText.text.toString().trim()

            if (botToken.isBlank()) {
                botTokenEditText.error = getString(R.string.bot_token_required)
                return@setOnClickListener
            }
            if (chatId.isBlank()) {
                chatIdEditText.error = getString(R.string.chat_id_required)
                return@setOnClickListener
            }

            AppConfig.saveTelegramCredentials(this, botToken, chatId)

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
