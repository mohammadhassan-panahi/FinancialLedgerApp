package com.example.domain.usecase

import com.example.crypto.analysis.TechnicalAnalysisResult
import com.example.data.local.CryptoAssetEntity
import com.example.data.repository.AiRepository

import java.util.Locale

class GetCryptoAIReportUseCase(
    private val aiRepository: AiRepository
) {
    /**
     * Generates a professional Persian AI analysis report for a cryptocurrency
     * based on its technical analysis results and current market data.
     */
    suspend operator fun invoke(
        analysis: TechnicalAnalysisResult,
        asset: CryptoAssetEntity
    ): String {
        val prompt = """
            به عنوان یک تحلیلگر تکنیکال حرفه‌ای بازار کریپتوکارنسی، یک گزارش تحلیلی برای ارز ${asset.name} با نماد ${asset.symbol} تهیه کن.
            
            اطلاعات فعلی بازار:
            - قیمت: ${asset.priceUsd} دلار
            - تغییرات ۲۴ ساعته: ${asset.percentChange24h}%
            
            یافته‌های تحلیل تکنیکال:
            - امتیاز فرصت: ${analysis.opportunityScore} از ۱۰۰
            - امتیاز ریسک: ${analysis.riskScore} از ۱۰۰
            - سیگنال فعلی: ${analysis.signal}
            - روند: ${analysis.trend}
            - وضعیت حجم: ${analysis.volumeTrend}
            - شاخص RSI: ${String.format(Locale.US, "%.2f", analysis.rsi)}
            - نقدینگی: ${analysis.liquidity}
            - سطح حمایتی: ${String.format(Locale.US, "%.2f", analysis.support)}
            - سطح مقاومتی: ${String.format(Locale.US, "%.2f", analysis.resistance)}
            
            دلایل و هشدارها:
            ${analysis.reasons.joinToString("\n")}
            ${analysis.warnings.joinToString("\n")}
            
            طرح معامله پیشنهادی:
            - محدوده ورود: ${analysis.entryZone?.let { "${it.first} تا ${it.second}" } ?: "نامشخص"}
            - حد ضرر: ${analysis.stopLoss ?: "نامشخص"}
            - حد سود: ${analysis.takeProfit ?: "نامشخص"}
            - نسبت سود به ریسک: ${analysis.riskReward ?: "نامشخص"}
            
            خروجی مورد انتظار:
            یک تحلیل فارسی روان، حرفه‌ای و خلاصه (حداکثر ۱۰۰ کلمه) که وضعیت روند، نقاط ورود/خروج احتمالی و سطح ریسک را توضیح دهد. لحن گزارش باید شبیه به تحلیلگران بازارهای مالی باشد.
        """.trimIndent()

        return aiRepository.analyzeDailyData(prompt)
    }
}
