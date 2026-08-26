package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiRepository
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
                _analysisResult.value = "خطا در جمع‌آوری داده‌ها: ${e.message}"
            } finally {
                _isLoading.value = false
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
