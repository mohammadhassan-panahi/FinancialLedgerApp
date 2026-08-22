package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculationHistoryDao
import com.example.data.local.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Backs the merged Calculators module (simple/compound interest, loan, inflation, gold/fx,
 * comparison, deposit calculators). Kept separate from [MarketPortfolioViewModel] on purpose:
 * the calculators only ever need calculation-history persistence, so giving them their own
 * small ViewModel avoids coupling them to the ledger/portfolio state and keeps this merge
 * low-risk for the rest of the app.
 */
class CalculatorViewModel(
    private val historyDao: CalculationHistoryDao
) : ViewModel() {

    fun getHistoryForSection(sectionKey: String): Flow<List<CalculationHistoryEntity>> =
        historyDao.getHistoryForSection(sectionKey)

    fun addHistory(history: CalculationHistoryEntity) {
        viewModelScope.launch {
            historyDao.insertHistory(history)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            historyDao.deleteHistoryById(id)
        }
    }

    fun clearSectionHistory(sectionKey: String) {
        viewModelScope.launch {
            historyDao.clearHistoryForSection(sectionKey)
        }
    }
}

class CalculatorViewModelFactory(
    private val historyDao: CalculationHistoryDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            return CalculatorViewModel(historyDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
