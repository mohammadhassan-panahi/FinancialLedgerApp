package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.usecase.ExportDataUseCase
import com.example.domain.usecase.ImportDataUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: UserPreferencesRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModel() {

    val currencyUnit: StateFlow<String> = repository.currencyUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TOMAN")

    val isPrivacyModeEnabled: StateFlow<Boolean> = repository.isPrivacyModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setCurrencyUnit(unit: String) {
        viewModelScope.launch {
            repository.setCurrencyUnit(unit)
        }
    }

    fun setPrivacyModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPrivacyModeEnabled(enabled)
        }
    }
}

class SettingsViewModelFactory(
    private val repository: UserPreferencesRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                repository,
                exportDataUseCase,
                importDataUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
