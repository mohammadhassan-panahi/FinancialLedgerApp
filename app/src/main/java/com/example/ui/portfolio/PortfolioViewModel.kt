package com.example.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.HoldingSummary
import com.example.data.repository.PortfolioRepository
import com.example.domain.usecase.CalculatePortfolioValueUseCase
import com.example.domain.usecase.GetAIInsightsUseCase
import com.example.domain.usecase.PortfolioSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for the Portfolio screen, driven by domain Use Cases.
 */
data class PortfolioUiState(
    val summary: PortfolioSummary? = null,
    val holdings: List<HoldingSummary> = emptyList(),
    val aiInsights: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class PortfolioViewModel(
    private val repository: PortfolioRepository,
    private val calculatePortfolioValueUseCase: CalculatePortfolioValueUseCase,
    private val getAIInsightsUseCase: GetAIInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        observePortfolioData()
    }

    private fun observePortfolioData() {
        combine(
            calculatePortfolioValueUseCase(),
            repository.holdings
        ) { summary, holdings ->
            _uiState.update { it.copy(summary = summary, holdings = holdings) }
        }.launchIn(viewModelScope)
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.refreshGoldAndDollar()
            repository.refreshIndices()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun requestAiInsights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val insights = getAIInsightsUseCase.execute()
                _uiState.update { it.copy(aiInsights = insights, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "خطا در دریافت تحلیل هوشمند: ${e.message}", isLoading = false) }
            }
        }
    }

    fun addPurchase(
        type: PortfolioAssetType,
        code: String,
        name: String,
        qty: Double,
        priceRial: Double,
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addPurchase(
                AssetPurchaseEntity(
                    assetType = type,
                    assetCode = code,
                    assetName = name,
                    quantity = qty,
                    unitPriceRial = priceRial,
                    totalPaidRial = qty * priceRial,
                    purchaseDate = date
                )
            )
        }
    }

    fun deletePurchase(id: Long) = viewModelScope.launch { repository.deletePurchase(id) }
}

class PortfolioViewModelFactory(
    private val repository: PortfolioRepository,
    private val calculateUseCase: CalculatePortfolioValueUseCase,
    private val aiUseCase: GetAIInsightsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(repository, calculateUseCase, aiUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
