package com.example.data.repository

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(private val apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        systemInstruction = content { 
            text("You are an Expert Financial Mentor. Provide professional, actionable, and concise financial advice in Persian. Analyze the user's portfolio data and give insights on risk, diversification, and potential actions based on market trends.") 
        },
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048
        }
    )

    private var chatSession: Chat? = null

    /**
     * Sends a message to the AI using a persistent ChatSession.
     * This allows for multi-turn conversations with context.
     */
    suspend fun getChatResponse(userMessage: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "خطا: کلید API تنظیم نشده است. لطفاً فایل .env را بررسی کنید."
        
        try {
            if (chatSession == null) {
                chatSession = generativeModel.startChat()
            }
            val response = chatSession?.sendMessage(userMessage)
            response?.text ?: "متأسفانه پاسخی دریافت نشد."
        } catch (e: Exception) {
            "خطا در ارتباط با هوش مصنوعی: ${e.message}"
        }
    }

    suspend fun getChatResponse(history: List<Pair<String, String>>, prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "خطا: کلید API تنظیم نشده است."
        
        try {
            val chatHistory = history.map { (user, model) ->
                content("user") { text(user) }
                content("model") { text(model) }
            }
            val chat = generativeModel.startChat(history = chatHistory)
            val response = chat.sendMessage(prompt)
            response.text ?: "متأسفانه پاسخی دریافت نشد."
        } catch (e: Exception) {
            "خطا در ارتباط با هوش مصنوعی: ${e.message}"
        }
    }

    suspend fun analyzeDailyData(data: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "خطا: کلید API تنظیم نشده است."
        
        try {
            val response = generativeModel.generateContent(
                content {
                    text("به عنوان یک تحلیلگر مالی خبره، داده‌های زیر را تحلیل کن و توصیه‌های کوتاه ارائه بده:\n\n$data")
                }
            )
            response.text ?: "متأسفانه تحلیلی دریافت نشد."
        } catch (e: Exception) {
            "خطا در تحلیل داده‌ها: ${e.message}"
        }
    }

    suspend fun scanInvoice(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "خطا: کلید API تنظیم نشده است."

        try {
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text("اطلاعات این فاکتور یا رسید خرید را استخراج کن. شامل مبلغ کل، تاریخ و نام فروشگاه.")
                }
            )
            response.text ?: "متأسفانه متنی از تصویر استخراج نشد."
        } catch (e: Exception) {
            "خطا در اسکن فاکتور: ${e.message}"
        }
    }
}
