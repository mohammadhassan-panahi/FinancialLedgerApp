package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.AssetSaleEntity
import com.example.data.local.PortfolioAssetType
import com.example.data.local.PriceAlertEntity
import com.example.data.repository.HoldingSummary
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(private val repository: PortfolioRepository) : ViewModel() {

    val holdings: StateFlow<List<HoldingSummary>> = repository.holdings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<AssetPurchaseEntity>> = repository.purchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<AssetSaleEntity>> = repository.sales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRealizedPnlRial: StateFlow<Double> = repository.totalRealizedPnlRial
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val marketRates = repository.marketRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val indices = repository.indices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts = repository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    val totalPortfolioValueRial: StateFlow<Double> = holdings
        .let { flow ->
            MutableStateFlow(0.0).also { out ->
                viewModelScope.launch {
                    flow.collect { list -> out.value = list.sumOf { it.currentValueRial } }
                }
            }
        }

    fun refreshAll(watchlistSymbols: List<String> = emptyList()) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val goldOk = repository.refreshGoldAndDollar()
            val indexOk = repository.refreshIndices()
            val stockOk = if (watchlistSymbols.isNotEmpty()) repository.refreshWatchlist(watchlistSymbols) else true
            _isOfflineMode.value = !goldOk && !indexOk && !stockOk
            _isRefreshing.value = false
        }
    }

    fun addPurchase(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantity: Double,
        unitPriceRial: Double,
        purchaseDate: Long,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addPurchase(
                AssetPurchaseEntity(
                    assetType = assetType,
                    assetCode = assetCode,
                    assetName = assetName,
                    quantity = quantity,
                    unitPriceRial = unitPriceRial,
                    totalPaidRial = quantity * unitPriceRial,
                    purchaseDate = purchaseDate
                )
            )
        }
    }

    fun deletePurchase(id: Long) = viewModelScope.launch { repository.deletePurchase(id) }

    private val _sellError = MutableStateFlow<String?>(null)
    val sellError: StateFlow<String?> = _sellError.asStateFlow()

    fun sellAsset(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantitySold: Double,
        saleUnitPriceRial: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.sellAsset(assetType, assetCode, assetName, quantitySold, saleUnitPriceRial)
                _sellError.value = null
                onSuccess()
            } catch (e: IllegalArgumentException) {
                _sellError.value = e.message
            }
        }
    }

    fun clearSellError() { _sellError.value = null }
    fun deleteSale(id: Long) = viewModelScope.launch { repository.deleteSale(id) }

    fun addAlert(alert: PriceAlertEntity) = viewModelScope.launch { repository.addAlert(alert) }
    fun deleteAlert(id: Long) = viewModelScope.launch { repository.deleteAlert(id) }

    fun addSymbolToWatchlist(symbol: String, fullName: String) =
        viewModelScope.launch { repository.addSymbolToWatchlist(symbol, fullName) }
}

class PortfolioViewModelFactory(private val repository: PortfolioRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
