package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.BankAccountEntity
import com.example.data.local.MarketRateEntity
import com.example.data.local.MutualFundEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.repository.FinancialRepository
import com.example.ui.components.BottomTab
import com.example.util.FinancialFormulas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarketPortfolioViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomTab.PORTFOLIO)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    val marketRates: StateFlow<List<MarketRateEntity>> = repository.marketRates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mutualFunds: StateFlow<List<MutualFundEntity>> = repository.mutualFunds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bankAccounts: StateFlow<List<BankAccountEntity>> = repository.allBankAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalLiquidity: StateFlow<Double> = repository.totalLiquidity
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Total calculated portfolio balance (Sum of deposits + market investments - expenses)
    // Now also includes actual bank liquidity
    val portfolioBalance: StateFlow<Double> = combine(
        transactions,
        marketRates,
        mutualFunds,
        totalLiquidity
    ) { txList, rates, funds, liquidity ->
        val initialCapital = 150000000.0 // Baseline capital
        val netTransactions = txList.sumOf { tx ->
            when (tx.type) {
                TransactionType.DEPOSIT -> tx.amount
                TransactionType.TRANSFER -> -tx.amount
                TransactionType.EXPENSE -> -tx.amount
                TransactionType.SWAP -> 0.0
            }
        }
        initialCapital + netTransactions + liquidity
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 150000000.0
    )

    val pnlPercentage: StateFlow<Double> = portfolioBalance.map { current ->
        val initial = 150000000.0
        FinancialFormulas.calculatePnLPercentage(initialCapital = initial, currentBalance = current)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val onlineSuccess = repository.refreshMarketData()
            _isOfflineMode.value = !onlineSuccess
            _isRefreshing.value = false
        }
    }

    fun addTransaction(title: String, amount: Double, type: TransactionType, category: String, accountId: Long? = null) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountId = accountId
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.removeTransaction(id)
        }
    }

    fun addBankAccount(name: String, bankName: String, initialBalance: Double, colorHex: String) {
        viewModelScope.launch {
            repository.addBankAccount(
                BankAccountEntity(
                    name = name,
                    bankName = bankName,
                    initialBalance = initialBalance,
                    currentBalance = initialBalance,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteBankAccount(account: BankAccountEntity) {
        viewModelScope.launch {
            repository.deleteBankAccount(account)
        }
    }

    fun selectTab(tab: BottomTab) {
        _selectedTab.value = tab
    }

    fun setAuthenticated(auth: Boolean) {
        _isAuthenticated.value = auth
    }
}

class MarketPortfolioViewModelFactory(
    private val repository: FinancialRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketPortfolioViewModel::class.java)) {
            return MarketPortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
