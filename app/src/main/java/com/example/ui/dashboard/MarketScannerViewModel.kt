package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.crypto.analysis.TechnicalAnalysisEngine
import com.example.crypto.analysis.TechnicalAnalysisResult
import com.example.data.local.CryptoAssetEntity
import com.example.data.repository.CryptoRepository
import com.example.domain.usecase.GetCryptoAIReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CryptoOpportunity(
    val asset: CryptoAssetEntity,
    val analysis: TechnicalAnalysisResult,
    val localReport: String,
    val aiReport: String? = null
)

class MarketScannerViewModel(
    private val cryptoRepository: CryptoRepository,
    private val getCryptoAIReportUseCase: GetCryptoAIReportUseCase
) : ViewModel() {

    private val _opportunities = MutableStateFlow<List<CryptoOpportunity>>(emptyList())
    val opportunities: StateFlow<List<CryptoOpportunity>> = _opportunities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedOpportunity = MutableStateFlow<CryptoOpportunity?>(null)
    val selectedOpportunity: StateFlow<CryptoOpportunity?> = _selectedOpportunity.asStateFlow()

    private val _aiReportLoading = MutableStateFlow(false)
    val aiReportLoading: StateFlow<Boolean> = _aiReportLoading.asStateFlow()

    init {
        scanMarket()
    }

    fun scanMarket() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Ensure we have fresh data for the top 10
            cryptoRepository.refreshTopListings(10)
            
            // 1. Get Top Assets
            val topAssets = cryptoRepository.allAssets.first().take(15)
            
            // 2. Fetch BTC Context First
            val btcHistory = cryptoRepository.fetchHistory("BTC", "1h").getOrNull()
            val btcAsset = topAssets.find { it.symbol == "BTC" }
            val btcContext = if (btcHistory != null && btcAsset != null) {
                val btcAnalysis = TechnicalAnalysisEngine.analyze("BTC", btcHistory, btcAsset)
                com.example.crypto.analysis.MarketContext(
                    isBullish = btcAnalysis.opportunityScore > 50,
                    volatility = (btcHistory.last().high - btcHistory.last().low) / btcHistory.last().close
                )
            } else null

            // 3. Scan All and Rank
            val results = topAssets.map { asset ->
                val historyResult = cryptoRepository.fetchHistory(asset.symbol, "1h")
                val candles = historyResult.getOrNull() ?: emptyList()
                val analysis = TechnicalAnalysisEngine.analyze(asset.symbol, candles, asset, btcContext)
                val localReport = generateLocalReport(analysis, asset)
                CryptoOpportunity(asset, analysis, localReport)
            }.sortedByDescending { it.analysis.opportunityScore }
            
            _opportunities.value = results
            _isLoading.value = false
        }
    }

    fun selectOpportunity(opportunity: CryptoOpportunity) {
        _selectedOpportunity.value = opportunity
    }

    fun fetchAiDeepAnalysis(opportunity: CryptoOpportunity) {
        if (opportunity.aiReport != null) return

        viewModelScope.launch {
            _aiReportLoading.value = true
            val report = getCryptoAIReportUseCase(opportunity.analysis, opportunity.asset)
            
            // Update the opportunity with the fetched report
            _opportunities.value = _opportunities.value.map {
                if (it.asset.symbol == opportunity.asset.symbol) {
                    it.copy(aiReport = report)
                } else it
            }
            
            // Also update selected one to show in UI immediately
            if (_selectedOpportunity.value?.asset?.symbol == opportunity.asset.symbol) {
                _selectedOpportunity.value = _selectedOpportunity.value?.copy(aiReport = report)
            }
            
            _aiReportLoading.value = false
        }
    }

    private fun generateLocalReport(analysis: TechnicalAnalysisResult, asset: CryptoAssetEntity): String {
        return buildString {
            appendLine("🔍 تحلیل دیدبان برای ${asset.symbol}:")
            appendLine("• روند قیمت: ${analysis.trend}")
            appendLine("• وضعیت حجم: ${analysis.volumeTrend}")
            appendLine("• شاخص RSI: ${analysis.rsi.toInt()}")
            appendLine()
            
            if (analysis.reasons.isNotEmpty()) {
                appendLine("✅ نقاط قوت:")
                analysis.reasons.forEach { appendLine("  - $it") }
            }
            
            if (analysis.warnings.isNotEmpty()) {
                appendLine("⚠️ هشدارها:")
                analysis.warnings.forEach { appendLine("  - $it") }
            }
            
            appendLine()
            appendLine("📊 سطوح کلیدی:")
            appendLine("  حمایت: $${String.format("%.2f", analysis.support)}")
            appendLine("  مقاومت: $${String.format("%.2f", analysis.resistance)}")
            
            analysis.entryZone?.let {
                appendLine()
                appendLine("🎯 طرح معامله:")
                appendLine("  محدوده ورود: $${String.format("%.2f", it.first)} - $${String.format("%.2f", it.second)}")
                appendLine("  حد ضرر (SL): $${String.format("%.2f", analysis.stopLoss)}")
                appendLine("  حد سود (TP): $${String.format("%.2f", analysis.takeProfit)}")
                appendLine("  نسبت سود به ریسک: ${String.format("%.2f", analysis.riskReward ?: 0.0)}")
            }
        }
    }

    fun clearSelection() {
        _selectedOpportunity.value = null
    }
}

class MarketScannerViewModelFactory(
    private val cryptoRepository: CryptoRepository,
    private val aiReportUseCase: GetCryptoAIReportUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketScannerViewModel(cryptoRepository, aiReportUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
