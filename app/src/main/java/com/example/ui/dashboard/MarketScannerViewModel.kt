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
            
            // Get the top 10 from local storage
            val topAssets = cryptoRepository.allAssets.first().take(10)
            
            val results = topAssets.map { asset ->
                val historyResult = cryptoRepository.fetchHistory(asset.symbol, "1h")
                val candles = historyResult.getOrNull() ?: emptyList()
                val analysis = TechnicalAnalysisEngine.analyze(asset.symbol, candles, asset)
                CryptoOpportunity(asset, analysis)
            }
            
            _opportunities.value = results
            _isLoading.value = false
        }
    }

    fun fetchAiReport(opportunity: CryptoOpportunity) {
        _selectedOpportunity.value = opportunity
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
            _selectedOpportunity.value = _selectedOpportunity.value?.copy(aiReport = report)
            
            _aiReportLoading.value = false
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
