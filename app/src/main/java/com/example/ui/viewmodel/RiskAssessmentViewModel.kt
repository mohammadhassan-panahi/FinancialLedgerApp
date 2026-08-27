package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NexFinRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RiskAssessmentViewModel(private val repository: NexFinRepository) : ViewModel() {

    val riskProfile = repository.riskProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun submitAssessment(score: Int) {
        val personality = when {
            score < 30 -> "محافظه‌کار (Conservative)"
            score < 70 -> "میانه‌رو (Balanced)"
            else -> "جسور (Aggressive)"
        }
        viewModelScope.launch {
            repository.saveRiskProfile(score, personality)
        }
    }
}

class RiskAssessmentViewModelFactory(private val repository: NexFinRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RiskAssessmentViewModel(repository) as T
    }
}
