package com.example.data.repository

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(private val apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048
        }
    )

    suspend fun analyzeDailyData(data: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "خطا: کلید API تنظیم نشده است. لطفاً فایل .env را بررسی کنید."
        
        try {
            val response = generativeModel.generateContent(
                content {
                    text("به عنوان یک تحلیلگر مالی خبره، داده‌های زیر که مربوط به تراکنش‌ها و دارایی‌های امروز کاربر است را تحلیل کن و توصیه‌های کوتاه و کاربردی ارائه بده. خروجی باید به زبان فارسی باشد:\n\n$data")
                }
            )
            response.text ?: "متأسفانه تحلیلی دریافت نشد."
        } catch (e: Exception) {
            "خطا در تحلیل داده‌ها: ${e.message}"
        }
    }

    suspend fun scanInvoice(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "خطا: کلید API تنظیم نشده است. لطفاً فایل .env را بررسی کنید."

        try {
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text("اطلاعات این فاکتور یا رسید خرید را استخراج کن. شامل مبلغ کل، تاریخ، نام فروشگاه و موارد خریداری شده. خروجی را به صورت یک ساختار خوانا و به زبان فارسی ارائه بده.")
                }
            )
            response.text ?: "متأسفانه متنی از تصویر استخراج نشد."
        } catch (e: Exception) {
            "خطا در اسکن فاکتور: ${e.message}"
        }
    }
}
