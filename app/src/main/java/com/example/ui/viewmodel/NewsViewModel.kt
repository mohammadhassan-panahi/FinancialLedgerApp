package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

    val cryptoNews = repository.getNews(NewsRepository.CATEGORY_CRYPTO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val iranEconomyNews = repository.getNews(NewsRepository.CATEGORY_IRAN_ECONOMY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshCryptoNews()
            repository.refreshIranEconomyNews()
            _isRefreshing.value = false
        }
    }
}

class NewsViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(repository) as T
    }
}
