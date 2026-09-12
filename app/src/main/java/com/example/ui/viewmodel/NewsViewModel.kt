package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.NewsEntity
import com.example.data.repository.AiRepository
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: NewsRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    val cryptoNews = repository.getNews(NewsRepository.CATEGORY_CRYPTO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val iranEconomyNews = repository.getNews(NewsRepository.CATEGORY_IRAN_ECONOMY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val techNews = repository.getNews(NewsRepository.CATEGORY_TECH)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedNews = MutableStateFlow<NewsEntity?>(null)
    val selectedNews: StateFlow<NewsEntity?> = _selectedNews.asStateFlow()

    fun selectNews(news: NewsEntity) {
        _selectedNews.value = news
        // Automatically request AI summary if not already present
        if (news.aiSummary == null) {
            summarizeNews(news)
        }
    }

    fun clearSelectedNews() {
        _selectedNews.value = null
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshAggregatedCryptoNews()
            repository.refreshIranEconomyNews()
            repository.refreshTechNews()
            _isRefreshing.value = false
        }
    }

    private fun summarizeNews(news: NewsEntity) {
        viewModelScope.launch {
            val summary = aiRepository.summarizeNews(news.title, news.description)
            repository.updateNewsSummary(news.id, summary)
            // Local update for UI if needed, but repository is the source of truth
            if (_selectedNews.value?.id == news.id) {
                _selectedNews.value = _selectedNews.value?.copy(aiSummary = summary)
            }
        }
    }
}

class NewsViewModelFactory(
    private val repository: NewsRepository,
    private val aiRepository: AiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(repository, aiRepository) as T
    }
}
