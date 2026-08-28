package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiRepository
import com.example.data.repository.HoldingSummary
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiAnalysisViewModel(
    private val aiRepository: AiRepository,
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

    private val _localAnalysis = MutableStateFlow<String?>(null)
    val localAnalysis: StateFlow<String?> = _localAnalysis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _ocrResult = MutableStateFlow<String?>(null)
    val ocrResult: StateFlow<String?> = _ocrResult.asStateFlow()

    fun analyzePortfolio() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val holdings = portfolioRepository.holdings.first()
                val totalValue = holdings.sumOf { it.currentValueRial }
                val accounts = portfolioRepository.bankAccounts.first()
                val totalLiquidity = accounts.sumOf { it.currentBalance }
                
                // Generate Local Logical Report
                _localAnalysis.value = generateLocalPortfolioReport(holdings, totalValue, totalLiquidity)
                
            } catch (e: Exception) {
                _localAnalysis.value = "خطا در تحلیل داده‌ها: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzePortfolioWithAi() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val holdings = portfolioRepository.holdings.first()
                val totalValue = holdings.sumOf { it.currentValueRial }
                val accounts = portfolioRepository.bankAccounts.first()
                val totalLiquidity = accounts.sumOf { it.currentBalance }

                val dataString = buildString {
                    appendLine("ارزش کل سبد دارایی: $totalValue ریال")
                    appendLine("کل نقدینگی در حساب‌ها: $totalLiquidity ریال")
                    appendLine("جزئیات دارایی‌ها:")
                    holdings.forEach {
                        appendLine("- ${it.assetName} (${it.assetCode}): مقدار ${it.quantity}، ارزش فعلی ${it.currentValueRial} ریال، سود/ضرر: ${it.profitLossRial} ریال")
                    }
                }

                _analysisResult.value = aiRepository.analyzeDailyData(dataString)
            } catch (e: Exception) {
                _analysisResult.value = "خطا در ارتباط با هوش مصنوعی: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateLocalPortfolioReport(
        holdings: List<HoldingSummary>,
        totalValue: Double,
        liquidity: Double
    ): String {
        return buildString {
            appendLine("📊 تحلیل وضعیت سبد دارایی (الگوریتم داخلی):")
            appendLine()
            
            if (holdings.isEmpty()) {
                appendLine("سبد دارایی شما فعلاً خالی است. برای شروع، اولین خرید خود را ثبت کنید.")
                return@buildString
            }

            val totalAssetCount = holdings.size
            val profitableAssets = holdings.count { it.profitLossRial > 0 }
            
            appendLine("• شما در حال حاضر $totalAssetCount نوع دارایی مختلف دارید.")
            appendLine("• تعداد $profitableAssets دارایی در وضعیت سوددهی هستند.")
            
            // Risk check: Diversification
            holdings.maxByOrNull { it.currentValueRial }?.let { maxAsset ->
                val ratio = (maxAsset.currentValueRial / totalValue) * 100
                if (ratio > 40) {
                    appendLine("⚠️ هشدار تمرکز سرمایه: دارایی '${maxAsset.assetName}' حدود ${ratio.toInt()}% از کل سبد شما را تشکیل می‌دهد. برای کاهش ریسک، تنوع بیشتری ایجاد کنید.")
                }
            }

            // Liquidity check
            val liquidityRatio = (liquidity / (totalValue + liquidity)) * 100
            if (liquidityRatio < 10) {
                appendLine("💡 پیشنهاد: ذخیره نقدینگی شما کمتر از ۱۰٪ است. داشتن نقدینگی کافی برای خرید در اصلاح‌های بازار ضروری است.")
            } else if (liquidityRatio > 50) {
                appendLine("💡 پیشنهاد: نقدینگی بالایی دارید (${liquidityRatio.toInt()}%). در صورت مشاهده فرصت خرید در دیدبان، بخشی از آن را وارد بازار کنید.")
            }

            // Overall health
            val totalProfit = holdings.sumOf { it.profitLossRial }
            if (totalProfit > 0) {
                appendLine("✅ وضعیت کلی سبد شما مثبت است. به استراتژی خود پایبند باشید.")
            } else {
                appendLine("🧐 وضعیت کلی سبد در ضرر است. نقاط خرید خود را بررسی کرده و در صورت نیاز با تحلیل تکنیکال میانگین کم کنید.")
            }
        }
    }

    fun scanInvoice(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _ocrResult.value = aiRepository.scanInvoice(bitmap)
            } catch (e: Exception) {
                _ocrResult.value = "خطا در اسکن: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearOcrResult() {
        _ocrResult.value = null
    }
}

class AiAnalysisViewModelFactory(
    private val aiRepository: AiRepository,
    private val portfolioRepository: PortfolioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiAnalysisViewModel::class.java)) {
            return AiAnalysisViewModel(aiRepository, portfolioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
