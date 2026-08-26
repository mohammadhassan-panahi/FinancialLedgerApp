package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.AssetSaleEntity
import com.example.data.local.BankAccountEntity
import com.example.data.local.PortfolioAssetType
import com.example.data.local.PriceAlertEntity
import com.example.data.repository.HoldingSummary
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(val repository: PortfolioRepository) : ViewModel() {

    val holdings: StateFlow<List<HoldingSummary>> = repository.holdings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccountEntity>> = repository.bankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLiquidityToman: StateFlow<Double> = repository.totalLiquidityRial
        .map { it / 10.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val purchases: StateFlow<List<AssetPurchaseEntity>> = repository.purchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<AssetSaleEntity>> = repository.sales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRealizedPnlRial: StateFlow<Double> = repository.totalRealizedPnlRial
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val marketRates: StateFlow<List<com.example.data.local.MarketRateEntity>> = repository.marketRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mutualFunds: StateFlow<List<com.example.data.local.MutualFundEntity>> = repository.mutualFunds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cryptoAssets: StateFlow<List<com.example.data.local.CryptoAssetEntity>> = repository.cryptoAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val indices = repository.indices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts = repository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debtCredits = repository.debtCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders = repository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals = repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ipos = repository.ipos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val codalNotices = repository.codalNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDebtRial: StateFlow<Double> = repository.totalDebtRial
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCreditRial: StateFlow<Double> = repository.totalCreditRial
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    val totalPortfolioValueRial: StateFlow<Double> = holdings
        .map { list -> list.sumOf { it.currentValueRial } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun refreshAll(watchlistSymbols: List<String> = emptyList()) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val goldOk = repository.refreshGoldAndDollar()
            val indexOk = repository.refreshIndices()
            val stockOk = if (watchlistSymbols.isNotEmpty()) repository.refreshWatchlist(watchlistSymbols) else true
            _isOfflineMode.value = !goldOk && !indexOk && !stockOk
            _isRefreshing.value = false
        }
    }

    fun addPurchase(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantity: Double,
        unitPriceRial: Double,
        purchaseDate: Long,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addPurchase(
                AssetPurchaseEntity(
                    assetType = assetType,
                    assetCode = assetCode,
                    assetName = assetName,
                    quantity = quantity,
                    unitPriceRial = unitPriceRial,
                    totalPaidRial = quantity * unitPriceRial,
                    purchaseDate = purchaseDate
                )
            )
        }
    }

    fun deletePurchase(id: Long) = viewModelScope.launch { repository.deletePurchase(id) }

    private val _sellError = MutableStateFlow<String?>(null)
    val sellError: StateFlow<String?> = _sellError.asStateFlow()

    fun sellAsset(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantitySold: Double,
        saleUnitPriceRial: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.sellAsset(assetType, assetCode, assetName, quantitySold, saleUnitPriceRial)
                _sellError.value = null
                onSuccess()
            } catch (e: IllegalArgumentException) {
                _sellError.value = e.message
            }
        }
    }

    fun clearSellError() { _sellError.value = null }
    fun deleteSale(id: Long) = viewModelScope.launch { repository.deleteSale(id) }

    fun addAlert(alert: PriceAlertEntity) = viewModelScope.launch { repository.addAlert(alert) }
    fun deleteAlert(id: Long) = viewModelScope.launch { repository.deleteAlert(id) }

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

    fun addSymbolToWatchlist(symbol: String, fullName: String) =
        viewModelScope.launch { repository.addSymbolToWatchlist(symbol, fullName) }

    fun removeSymbolFromWatchlist(symbol: String) =
        viewModelScope.launch { repository.removeSymbolFromWatchlist(symbol) }

    // Debt & Credit
    fun addDebtCredit(personName: String, amountRial: Double, type: com.example.data.local.DebtCreditType, description: String = "") {
        viewModelScope.launch {
            repository.addDebtCredit(
                com.example.data.local.DebtCreditEntity(
                    personName = personName,
                    amountRial = amountRial,
                    type = type,
                    description = description
                )
            )
        }
    }

    fun deleteDebtCredit(entity: com.example.data.local.DebtCreditEntity) = viewModelScope.launch { repository.deleteDebtCredit(entity) }
    fun settleDebtCredit(entity: com.example.data.local.DebtCreditEntity) = viewModelScope.launch { repository.updateDebtCredit(entity.copy(isSettled = true)) }

    // Reminders
    fun addReminder(title: String, amountRial: Double, type: com.example.data.local.ReminderType, dueDate: Long, note: String = "") {
        viewModelScope.launch {
            repository.addReminder(
                com.example.data.local.ReminderEntity(
                    title = title,
                    amountRial = amountRial,
                    type = type,
                    dueDate = dueDate,
                    note = note
                )
            )
        }
    }

    fun deleteReminder(entity: com.example.data.local.ReminderEntity) = viewModelScope.launch { repository.deleteReminder(entity) }
    fun markReminderAsPaid(entity: com.example.data.local.ReminderEntity) = viewModelScope.launch { repository.updateReminder(entity.copy(isPaid = true)) }

    // Goals
    fun addGoal(title: String, targetAmountRial: Double, category: String = "سایر") {
        viewModelScope.launch {
            repository.addGoal(com.example.data.local.GoalEntity(title = title, targetAmountRial = targetAmountRial, category = category))
        }
    }
    fun updateGoalProgress(entity: com.example.data.local.GoalEntity, savedAmount: Double) {
        viewModelScope.launch {
            repository.updateGoal(entity.copy(currentSavedRial = savedAmount, isCompleted = savedAmount >= entity.targetAmountRial))
        }
    }
    fun deleteGoal(entity: com.example.data.local.GoalEntity) = viewModelScope.launch { repository.deleteGoal(entity) }
}

class PortfolioViewModelFactory(private val repository: PortfolioRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
