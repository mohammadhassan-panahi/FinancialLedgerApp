package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NexFinRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SocialHubViewModel(private val repository: NexFinRepository) : ViewModel() {

    val socialFeed = repository.socialFeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedSampleSocialPosts()
        }
    }

    fun postAnalysis(content: String, assetCode: String? = null, sentiment: String? = null) {
        viewModelScope.launch {
            repository.shareAnalysis(content, assetCode, sentiment)
        }
    }
}

class SocialHubViewModelFactory(private val repository: NexFinRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SocialHubViewModel(repository) as T
    }
}
