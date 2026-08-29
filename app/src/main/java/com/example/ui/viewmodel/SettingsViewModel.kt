package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    val currencyUnit: StateFlow<String> = repository.currencyUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TOMAN")

    fun setCurrencyUnit(unit: String) {
        viewModelScope.launch {
            repository.setCurrencyUnit(unit)
        }
    }
}

class SettingsViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
