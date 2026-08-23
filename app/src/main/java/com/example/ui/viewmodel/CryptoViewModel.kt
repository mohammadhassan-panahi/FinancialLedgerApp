package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CryptoAssetEntity
import com.example.data.repository.CryptoRepository
import com.example.data.repository.GlobalMarketSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CryptoViewModel(private val repository: CryptoRepository) : ViewModel() {

    val allAssets: StateFlow<List<CryptoAssetEntity>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<CryptoAssetEntity>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _globalSnapshot = MutableStateFlow<GlobalMarketSnapshot?>(null)
    val globalSnapshot: StateFlow<GlobalMarketSnapshot?> = _globalSnapshot.asStateFlow()

    fun refreshMarketData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            val listingsResult = repository.refreshTopListings(limit = 100)
            if (listingsResult.isFailure) {
                _error.value = listingsResult.exceptionOrNull()?.message
            }
            
            val metricsResult = repository.fetchGlobalMetrics()
            if (metricsResult.isSuccess) {
                _globalSnapshot.value = metricsResult.getOrNull()
            }
            
            _isRefreshing.value = false
        }
    }

    fun toggleWatchlist(asset: CryptoAssetEntity) {
        viewModelScope.launch {
            if (asset.isInWatchlist) {
                repository.removeFromWatchlist(asset.symbol)
            } else {
                repository.addToWatchlist(asset.symbol)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
