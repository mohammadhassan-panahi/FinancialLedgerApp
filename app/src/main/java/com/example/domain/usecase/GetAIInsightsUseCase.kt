package com.example.domain.usecase

import com.example.data.repository.AiRepository
import kotlinx.coroutines.flow.first

/**
 * Uses PortfolioSummary to generate a personalized financial analysis prompt 
 * and retrieves insights from the AI mentor.
 */
class GetAIInsightsUseCase(
    private val calculatePortfolioValueUseCase: CalculatePortfolioValueUseCase,
    private val aiRepository: AiRepository
) {
    suspend fun execute(): String {
        val summary = calculatePortfolioValueUseCase().first()
        
        val prompt = StringBuilder()
        prompt.append("تحلیل پرتفوی مالی من را انجام بده:\n")
        prompt.append("- ارزش کل دارایی‌ها: ${String.format("%,.0f", summary.totalRial)} ریال\n")
        prompt.append("- معادل دلاری: ${String.format("%.2f", summary.totalUsdt)} تتر (USDT)\n")
        prompt.append("- تغییرات امروز: ${String.format("%,.0f", summary.dailyChangeRial)} ریال (${String.format("%.2f", summary.dailyChangePercent)}%)\n")
        prompt.append("- ترکیب دارایی‌ها:\n")
        
        summary.assetBreakdown.forEach { (type, value) ->
            val percentage = if (summary.totalRial > 0) (value / summary.totalRial) * 100.0 else 0.0
            prompt.append("  * ${type.name}: ${String.format("%.1f", percentage)}%\n")
        }
        
        prompt.append("\nلطفاً با توجه به این داده‌ها، وضعیت ریسک، تنوع‌بخشی و تورم را تحلیل کن و پیشنهاداتی برای بهینه‌سازی سبد دارایی من ارائه بده. پاسخ کوتاه و کاربردی باشد.")

        return aiRepository.getChatResponse(prompt.toString())
    }
}
