package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.PortfolioRepository
import com.example.domain.model.Holding
import com.example.domain.model.PortfolioSummary
import com.example.domain.usecase.AddAssetPurchaseUseCase
import com.example.domain.usecase.GetHoldingsUseCase
import com.example.domain.usecase.GetPortfolioSummaryUseCase
import com.example.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val repository: PortfolioRepository,
    private val getHoldingsUseCase: GetHoldingsUseCase,
    private val getPortfolioSummaryUseCase: GetPortfolioSummaryUseCase,
    private val addAssetPurchaseUseCase: AddAssetPurchaseUseCase
) : ViewModel() {

    val holdingsState: StateFlow<UiState<List<Holding>>> = getHoldingsUseCase()
        .map { UiState.Success(it) as UiState<List<Holding>> }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val summaryState: StateFlow<UiState<PortfolioSummary?>> = getPortfolioSummaryUseCase()
        .map { UiState.Success(it) as UiState<PortfolioSummary?> }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // Simplified flows for legacy screens
    val holdings: StateFlow<List<Holding>> = getHoldingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioSummary: StateFlow<PortfolioSummary?> = getPortfolioSummaryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val snapshots: StateFlow<List<PortfolioSnapshotEntity>> = repository.snapshots
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

    val vehicles: StateFlow<List<com.example.data.local.VehicleEntity>> = repository.vehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val realEstates: StateFlow<List<com.example.data.local.RealEstateEntity>> = repository.realEstates
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

    val goldPriceToman: StateFlow<Double> = repository.marketRates
        .map { rates -> rates.find { it.assetCode == "GOLD_18K" }?.priceToman ?: 3500000.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3500000.0)

    val usdPriceToman: StateFlow<Double> = repository.marketRates
        .map { rates -> rates.find { it.assetCode == "USD" }?.priceToman ?: 65000.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 65000.0)

    val assetAllocations: StateFlow<Map<PortfolioAssetType, Double>> = holdings
        .map { list -> 
            list.groupBy { it.assetType }
                .mapValues { it.value.sumOf { h -> h.currentValueRial } / 10.0 } 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    val totalPortfolioValueRial: StateFlow<Double> = holdings
        .map { list: List<Holding> -> list.sumOf { it.currentValueRial } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun refreshAll(watchlistSymbols: List<String> = emptyList()) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val goldOk = repository.refreshGoldAndDollar()
            val indexOk = repository.refreshIndices()
            val stockOk = if (watchlistSymbols.isNotEmpty()) repository.refreshWatchlist(watchlistSymbols) else true
            
            // Capture a snapshot after a successful refresh
            if (goldOk || indexOk || stockOk) {
                repository.saveSnapshot()
            }
            
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
            addAssetPurchaseUseCase(
                assetType = assetType,
                assetCode = assetCode,
                assetName = assetName,
                quantity = quantity,
                unitPriceRial = unitPriceRial,
                purchaseDate = purchaseDate
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

class PortfolioViewModelFactory(
    private val repository: PortfolioRepository,
    private val getHoldingsUseCase: GetHoldingsUseCase,
    private val getPortfolioSummaryUseCase: GetPortfolioSummaryUseCase,
    private val addAssetPurchaseUseCase: AddAssetPurchaseUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(
                repository,
                getHoldingsUseCase,
                getPortfolioSummaryUseCase,
                addAssetPurchaseUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
