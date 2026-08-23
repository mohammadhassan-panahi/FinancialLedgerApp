package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MarketRateEntity
import com.example.data.local.MutualFundEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.repository.FinancialRepository
import com.example.util.FinancialFormulas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PortfolioUiState(
    val totalBalance: Double = 150000000.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val pnlPercentage: Double = 0.0,
    val isOfflineMode: Boolean = false,
    val isRefreshing: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val marketRates: List<MarketRateEntity> = emptyList(),
    val mutualFunds: List<MutualFundEntity> = emptyList()
)

class MainLedgerViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<PortfolioUiState> = combine(
        combine(repository.allTransactions, repository.totalIncome, repository.totalExpenses) { txList, income, expenses ->
            Triple(txList, income ?: 0.0, expenses ?: 0.0)
        },
        combine(repository.marketRates, repository.mutualFunds) { rates, funds ->
            Pair(rates, funds)
        },
        _isOfflineMode,
        _isRefreshing
    ) { (txList, totalIncomeVal, totalExpenseVal), (rates, funds), offline, refreshing ->
        val baseCapital = 150000000.0
        val currentBalance = baseCapital + totalIncomeVal - totalExpenseVal
        val pnl = FinancialFormulas.calculatePnLPercentage(baseCapital, currentBalance)

        PortfolioUiState(
            totalBalance = currentBalance,
            totalIncome = totalIncomeVal,
            totalExpenses = totalExpenseVal,
            pnlPercentage = pnl,
            isOfflineMode = offline,
            isRefreshing = refreshing,
            transactions = txList,
            marketRates = rates,
            mutualFunds = funds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioUiState()
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val success = repository.refreshMarketData()
            _isOfflineMode.value = !success
            _isRefreshing.value = false
        }
    }

    fun addTransaction(title: String, amount: Double, type: TransactionType, category: String, note: String = "") {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                note = note
            )
            repository.addTransaction(entity)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.removeTransaction(id)
        }
    }
}

class MainLedgerViewModelFactory(
    private val repository: FinancialRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainLedgerViewModel::class.java)) {
            return MainLedgerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
