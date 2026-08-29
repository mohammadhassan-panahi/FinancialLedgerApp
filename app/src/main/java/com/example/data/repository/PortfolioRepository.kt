package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.MarketApiService
import com.example.data.remote.TsetmcApiClient
import com.example.data.remote.TsetmcApiService
import com.example.domain.model.AllocationItem
import com.example.domain.model.GoldPriceAnalysis
import com.example.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** One row of the holdings summary: everything the user owns of one asset. */
data class HoldingSummary(
    val assetType: PortfolioAssetType,
    val assetCode: String,
    val assetName: String,
    val quantity: Double,
    val totalPaidRial: Double,
    val currentPriceRial: Double,
    val currentValueRial: Double,
    val profitLossRial: Double,
    val profitLossPercent: Double,
    val dailyChangePercent: Double = 0.0,
    val dailyChangeRial: Double = 0.0,
    val inflationAdjustedProfitLossRial: Double = 0.0,
    val cmcId: Int? = null
)

class PortfolioRepository(
    private val purchaseDao: AssetPurchaseDao,
    private val saleDao: AssetSaleDao,
    private val bankAccountDao: BankAccountDao,
    private val marketDao: MarketDao,
    private val stockDao: StockDao,
    private val alertDao: PriceAlertDao,
    private val debtCreditDao: DebtCreditDao,
    private val reminderDao: ReminderDao,
    private val goalDao: GoalDao,
    private val cryptoDao: CryptoDao,
    private val bourseDao: BourseDao,
    private val vehicleDao: VehicleDao,
    private val realEstateDao: RealEstateDao,
    private val snapshotDao: PortfolioSnapshotDao,
    private val apiKey: String = "",
    private val marketApiService: MarketApiService? = if (apiKey.isNotBlank()) MarketApiService.create() else null,
    private val tsetmcApiService: TsetmcApiClient? = if (apiKey.isNotBlank()) TsetmcApiClient(TsetmcApiService.create(), apiKey) else null
) {
    companion object {
        const val RIAL_PER_TOMAN = 10.0
    }

    val purchases: Flow<List<AssetPurchaseEntity>> = purchaseDao.getAllPurchases()
    val sales: Flow<List<AssetSaleEntity>> = saleDao.getAllSales()
    val marketRates: Flow<List<MarketRateEntity>> = marketDao.getAllMarketRates()
    val mutualFunds: Flow<List<MutualFundEntity>> = marketDao.getAllMutualFunds()
    val watchlist: Flow<List<StockSymbolEntity>> = stockDao.getWatchlist()
    val indices: Flow<List<MarketIndexEntity>> = stockDao.getIndices()
    val alerts: Flow<List<PriceAlertEntity>> = alertDao.getAllAlerts()
    val bankAccounts: Flow<List<BankAccountEntity>> = bankAccountDao.getAllAccounts()
    val cryptoAssets: Flow<List<CryptoAssetEntity>> = cryptoDao.getAllAssets()
    val vehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val realEstates: Flow<List<RealEstateEntity>> = realEstateDao.getAllProperties()
    val debtCredits: Flow<List<DebtCreditEntity>> = debtCreditDao.getAll()
    val reminders: Flow<List<ReminderEntity>> = reminderDao.getAll()
    val goals: Flow<List<GoalEntity>> = goalDao.getAll()
    val ipos: Flow<List<IpoEntity>> = bourseDao.getAllIpos()
    val codalNotices: Flow<List<CodalEntity>> = bourseDao.getAllCodalNotices()
    val snapshots: Flow<List<PortfolioSnapshotEntity>> = snapshotDao.getAllSnapshots()

    val totalDebtRial: Flow<Double> = debtCreditDao.getTotalDebt().map { it ?: 0.0 }
    val totalCreditRial: Flow<Double> = debtCreditDao.getTotalCredit().map { it ?: 0.0 }
    val totalRealizedPnlRial: Flow<Double> = sales.map { list -> list.sumOf { it.realizedPnlRial } }
    val totalLiquidityRial: Flow<Double> = bankAccountDao.getTotalLiquidity().map { (it ?: 0.0) * RIAL_PER_TOMAN }

    /**
     * Combines all data sources to provide a unified view of the user's portfolio.
     */
    val holdings: Flow<List<HoldingSummary>> = combine(
        purchases,
        sales,
        marketRates,
        mutualFunds,
        watchlist,
        cryptoAssets,
        totalLiquidityRial,
        vehicles,
        realEstates
    ) { array ->
        val txns = array[0] as List<AssetPurchaseEntity>
        val soldTxns = array[1] as List<AssetSaleEntity>
        val rates = array[2] as List<MarketRateEntity>
        val funds = array[3] as List<MutualFundEntity>
        val stocks = array[4] as List<StockSymbolEntity>
        val cryptos = array[5] as List<CryptoAssetEntity>
        val liquidityRial = array[6] as Double
        val vehicleRates = array[7] as List<VehicleEntity>
        val propertyRates = array[8] as List<RealEstateEntity>
        
        val result = mutableListOf<HoldingSummary>()
        val usdRateToman = rates.find { it.assetCode == "USD" }?.priceToman ?: 60000.0
        val usdToRial = usdRateToman * RIAL_PER_TOMAN

        if (liquidityRial > 0) {
            result.add(HoldingSummary(PortfolioAssetType.CASH, "CASH_RIAL", "نقدینگی", liquidityRial, liquidityRial, 1.0, liquidityRial, 0.0, 0.0))
        }

        val soldByCode = soldTxns.groupBy { it.assetCode }
        txns.groupBy { it.assetCode }.forEach { (code, group) ->
            val type = group.first().assetType
            val purchasedQty = group.sumOf { it.quantity }
            val purchasedCost = group.sumOf { it.totalPaidRial }
            val soldQty = soldByCode[code]?.sumOf { it.quantitySold } ?: 0.0
            val soldCostBasis = soldByCode[code]?.sumOf { it.costBasisRial } ?: 0.0

            val quantity = purchasedQty - soldQty
            val totalPaid = purchasedCost - soldCostBasis
            if (quantity <= 0.0001) return@forEach

            val currentPriceRial: Double
            val dailyChangePercent: Double
            
            val stockMatch = stocks.find { it.symbol == code }
            val rateMatch = rates.find { it.assetCode == code }
            val cryptoMatch = cryptos.find { it.symbol == code }
            val fundMatch = funds.find { it.id == code }
            val vehicleMatch = vehicleRates.find { it.modelName == group.first().assetName || it.modelName == code }
            val propertyMatch = propertyRates.find { it.propertyName == group.first().assetName || it.propertyName == code }

            when {
                type == PortfolioAssetType.STOCK && stockMatch != null -> {
                    currentPriceRial = stockMatch.lastPriceRial
                    dailyChangePercent = stockMatch.changePercent
                }
                type == PortfolioAssetType.CRYPTO && cryptoMatch != null -> {
                    currentPriceRial = (cryptoMatch.priceUsd ?: 0.0) * usdToRial
                    dailyChangePercent = cryptoMatch.percentChange24h ?: 0.0
                }
                type == PortfolioAssetType.FUND && fundMatch != null -> {
                    currentPriceRial = fundMatch.navToman * RIAL_PER_TOMAN
                    dailyChangePercent = fundMatch.returnPercent / 30.0
                }
                type == PortfolioAssetType.VEHICLE && vehicleMatch != null -> {
                    currentPriceRial = vehicleMatch.priceRial
                    dailyChangePercent = vehicleMatch.changePercent
                }
                type == PortfolioAssetType.REAL_ESTATE && propertyMatch != null -> {
                    currentPriceRial = propertyMatch.valuationRial
                    dailyChangePercent = propertyMatch.changePercent
                }
                rateMatch != null -> {
                    currentPriceRial = rateMatch.priceToman * RIAL_PER_TOMAN
                    dailyChangePercent = rateMatch.changePercent
                }
                else -> {
                    currentPriceRial = if (quantity > 0) totalPaid / quantity else 0.0
                    dailyChangePercent = 0.0
                }
            }

            val currentValue = quantity * currentPriceRial
            val pnl = currentValue - totalPaid
            val dailyChangeRial = if (dailyChangePercent != 0.0) currentValue * (1.0 - 1.0 / (1.0 + dailyChangePercent / 100.0)) else 0.0
            
            // Assume 40% annual inflation for "Real Growth" calculation (~2.8% monthly)
            val inflationRate = 0.40 
            val purchaseDate = group.first().purchaseDate
            val yearsPassed = (System.currentTimeMillis() - purchaseDate).toDouble() / (1000.0 * 60 * 60 * 24 * 365)
            val inflationFactor = Math.pow(1.0 + inflationRate, yearsPassed)
            val inflationAdjustedPaid = totalPaid * inflationFactor
            val realPnl = currentValue - inflationAdjustedPaid

            result.add(
                HoldingSummary(
                    assetType = type,
                    assetCode = code,
                    assetName = group.first().assetName,
                    quantity = quantity,
                    totalPaidRial = totalPaid,
                    currentPriceRial = currentPriceRial,
                    currentValueRial = currentValue,
                    profitLossRial = pnl,
                    profitLossPercent = if (totalPaid > 0) (pnl / totalPaid) * 100.0 else 0.0,
                    dailyChangePercent = dailyChangePercent,
                    dailyChangeRial = dailyChangeRial,
                    inflationAdjustedProfitLossRial = realPnl,
                    cmcId = cryptoMatch?.cmcId
                )
            )
        }
        result
    }

    /**
     * Aggregated summary of the entire portfolio.
     */
    val portfolioSummary: Flow<PortfolioSummary> = combine(holdings, marketRates) { list, rates ->
        val totalValue = list.sumOf { it.currentValueRial }
        val totalPaid = list.sumOf { it.totalPaidRial }
        val todayChange = list.sumOf { it.dailyChangeRial }

        val usdRateToman = rates.find { it.assetCode == "USD" }?.priceToman ?: 65000.0
        val gold18kRateToman = rates.find { it.assetCode.contains("GOLD_18K") || it.name.contains("۱۸") }?.priceToman ?: 3500000.0

        val usdRateRial = usdRateToman * RIAL_PER_TOMAN
        val gold18kRateRial = gold18kRateToman * RIAL_PER_TOMAN

        val totalPnl = totalValue - totalPaid
        val totalPnlPercent = if (totalPaid > 0) (totalPnl / totalPaid) * 100.0 else 0.0
        val todayPnlPercent = if ((totalValue - todayChange) > 0) (todayChange / (totalValue - todayChange)) * 100.0 else 0.0

        val marketStatus = when {
            todayPnlPercent > 1.5 -> "بازار صعودی قدرتمند"
            todayPnlPercent > 0.5 -> "بازار مثبت"
            todayPnlPercent < -1.5 -> "بازار ریزشی شدید"
            todayPnlPercent < -0.5 -> "بازار منفی"
            else -> "بازار متعادل"
        }

        val best = list.filter { it.assetType != PortfolioAssetType.CASH }.maxByOrNull { it.dailyChangePercent }
        val worst = list.filter { it.assetType != PortfolioAssetType.CASH }.minByOrNull { it.dailyChangePercent }

        val byAsset = list.map {
            AllocationItem(it.assetName, if (totalValue > 0) (it.currentValueRial / totalValue) * 100 else 0.0, it.currentValueRial)
        }.sortedByDescending { it.valueRial }

        val byType = list.groupBy { it.assetType }.map { (type, group) ->
            val typeValue = group.sumOf { it.currentValueRial }
            val label = when(type) {
                PortfolioAssetType.GOLD -> "طلا و مسکوکات"
                PortfolioAssetType.USD -> "ارزهای خارجی"
                PortfolioAssetType.STOCK -> "سهام بورس"
                PortfolioAssetType.CRYPTO -> "رمزارزها"
                PortfolioAssetType.CASH -> "نقدینگی"
                PortfolioAssetType.FUND -> "صندوق‌های سرمایه‌گذاری"
                PortfolioAssetType.REAL_ESTATE -> "املاک"
                PortfolioAssetType.VEHICLE -> "خودرو"
            }
            AllocationItem(label, if (totalValue > 0) (typeValue / totalValue) * 100 else 0.0, typeValue)
        }.sortedByDescending { it.valueRial }

        val globalGold = rates.find { it.assetCode.contains("XAU") || it.name.contains("انس") }
        val usdRate = rates.find { it.assetCode == "USD" }
        val localGold = rates.find { it.assetCode.contains("GOLD_18K") || it.name.contains("۱۸") }

        val goldAnalysis = if (globalGold != null && usdRate != null && localGold != null) {
            val driver = if (Math.abs(globalGold.changePercent) > Math.abs(usdRate.changePercent)) "GLOBAL_GOLD" else "USD"
            GoldPriceAnalysis(
                globalGoldChangePercent = globalGold.changePercent,
                usdChangePercent = usdRate.changePercent,
                localGoldChangePercent = localGold.changePercent,
                primaryDriver = driver
            )
        } else null

        val insights = mutableListOf<String>()
        best?.let { insights.add("بهترین دارایی امروز شما ${it.assetName} با ${String.format(Locale.US, "%.1f", it.dailyChangePercent)}٪ رشد بوده است.") }
        byAsset.firstOrNull()?.let { insights.add("بیشترین سهم پورتفوی شما متعلق به ${it.label} است (${String.format(Locale.US, "%.1f", it.percentage)}٪).") }

        val goldEffect = list.find { it.assetType == PortfolioAssetType.GOLD }?.dailyChangeRial ?: 0.0
        val totalAbsChange = Math.abs(todayChange)
        if (totalAbsChange > 0 && Math.abs(goldEffect) / totalAbsChange > 0.5) {
            insights.add("تغییرات قیمت طلا بیشترین تأثیر را روی ارزش پورتفوی شما در امروز داشته است.")
        }

        val staleHours = (System.currentTimeMillis() - (rates.maxOfOrNull { it.updatedAt } ?: 0L)) / (1000 * 60 * 60)
        if (staleHours > 3) {
            insights.add("⚠️ قیمت‌های بازار بیش از ${com.example.util.PersianNumberUtils.toPersianDigits(staleHours.toString())} ساعت است که بروزرسانی نشده‌اند.")
        }

        PortfolioSummary(
            totalValueRial = totalValue,
            totalProfitLossRial = totalPnl,
            totalProfitLossPercent = totalPnlPercent,
            todayProfitLossRial = todayChange,
            todayProfitLossPercent = todayPnlPercent,
            lastUpdated = rates.maxOfOrNull { it.updatedAt } ?: System.currentTimeMillis(),
            marketStatus = marketStatus,
            usdRateRial = usdRateRial,
            gold18kPriceRial = gold18kRateRial,
            bestPerformer = best,
            worstPerformer = worst,
            allocationByAsset = byAsset,
            allocationByType = byType,
            goldAnalysis = goldAnalysis,
            insights = insights
        )
    }

    suspend fun saveSnapshot() {
        val summary = portfolioSummary.first()
        val moshi = com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(List::class.java) // Simple way for now

        // More robust: use specific types or a wrapper
        val assetJson = moshi.adapter(List::class.java).toJson(summary.allocationByAsset)
        val typeJson = moshi.adapter(List::class.java).toJson(summary.allocationByType)

        val snapshot = PortfolioSnapshotEntity(
            totalValueRial = summary.totalValueRial,
            totalProfitLossRial = summary.totalProfitLossRial,
            goldPriceRial = summary.gold18kPriceRial,
            usdPriceRial = summary.usdRateRial,
            stockIndexValue = indices.first().find { it.indexCode == "TOTAL_INDEX" || it.name.contains("کل") }?.value ?: 0.0,
            allocationByAssetJson = assetJson,
            allocationByTypeJson = typeJson
        )
        snapshotDao.insertSnapshot(snapshot)
    }

    suspend fun addPurchase(purchase: AssetPurchaseEntity) = purchaseDao.insertPurchase(purchase)
    suspend fun deletePurchase(id: Long) = purchaseDao.deletePurchase(id)

    suspend fun sellAsset(assetType: PortfolioAssetType, assetCode: String, assetName: String, quantitySold: Double, saleUnitPriceRial: Double, saleDate: Long = System.currentTimeMillis()): AssetSaleEntity {
        val allPurchases = purchases.first().filter { it.assetCode == assetCode && it.assetType == assetType }
        val allSales = sales.first().filter { it.assetCode == assetCode && it.assetType == assetType }
        val remainingQty = allPurchases.sumOf { it.quantity } - allSales.sumOf { it.quantitySold }
        val remainingCost = allPurchases.sumOf { it.totalPaidRial } - allSales.sumOf { it.costBasisRial }
        
        require(quantitySold <= remainingQty + 0.0001) { "موجودی کافی نیست" }
        val costBasis = (remainingCost / remainingQty) * quantitySold
        val sale = AssetSaleEntity(
            assetType = assetType,
            assetCode = assetCode,
            assetName = assetName,
            quantitySold = quantitySold,
            saleUnitPriceRial = saleUnitPriceRial,
            totalReceivedRial = quantitySold * saleUnitPriceRial,
            costBasisRial = costBasis,
            realizedPnlRial = (quantitySold * saleUnitPriceRial) - costBasis,
            saleDate = saleDate
        )
        saleDao.insertSale(sale)
        return sale
    }

    suspend fun deleteSale(id: Long) = saleDao.deleteSale(id)
    suspend fun addAlert(alert: PriceAlertEntity) = alertDao.insertAlert(alert)
    suspend fun deleteAlert(id: Long) = alertDao.deleteAlert(id)
    suspend fun addBankAccount(account: BankAccountEntity) = bankAccountDao.insertAccount(account)
    suspend fun deleteBankAccount(account: BankAccountEntity) = bankAccountDao.deleteAccount(account)
    suspend fun updateBankAccount(account: BankAccountEntity) = bankAccountDao.updateAccount(account)
    suspend fun addDebtCredit(entity: DebtCreditEntity) = debtCreditDao.insert(entity)
    suspend fun updateDebtCredit(entity: DebtCreditEntity) = debtCreditDao.update(entity)
    suspend fun deleteDebtCredit(entity: DebtCreditEntity) = debtCreditDao.delete(entity)
    suspend fun addReminder(entity: ReminderEntity) = reminderDao.insert(entity)
    suspend fun updateReminder(entity: ReminderEntity) = reminderDao.update(entity)
    suspend fun deleteReminder(entity: ReminderEntity) = reminderDao.delete(entity)
    suspend fun addGoal(entity: GoalEntity) = goalDao.insert(entity)
    suspend fun updateGoal(entity: GoalEntity) = goalDao.update(entity)
    suspend fun deleteGoal(entity: GoalEntity) = goalDao.delete(entity)

    suspend fun refreshGoldAndDollar(): Boolean {
        val response = marketApiService?.getGoldCurrency(apiKey) ?: return false
        return if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            val liveRates = (body.gold + body.currency).map {
                MarketRateEntity(
                    assetCode = it.symbol,
                    name = it.name,
                    priceToman = if (it.unit == "تومان") it.price else 0.0,
                    priceGlobal = if (it.unit != "تومان") it.price else 0.0,
                    currency = it.unit,
                    changePercent = it.changePercent,
                    isOfflineRate = false
                )
            }
            if (liveRates.isNotEmpty()) marketDao.insertMarketRates(liveRates)
            true
        } else false
    }

    suspend fun refreshWatchlist(symbols: List<String>): Boolean {
        val response = tsetmcApiService?.getAllSymbols() ?: return false
        return if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            symbols.forEach { sym ->
                body.find { it.symbol == sym }?.let {
                    stockDao.insertSymbol(StockSymbolEntity(it.symbol!!, it.fullName!!, it.closingPrice ?: 0.0, it.changePercent ?: 0.0))
                }
            }
            true
        } else false
    }

    suspend fun refreshIndices(): Boolean {
        val response = tsetmcApiService?.getIndices() ?: return false
        return if (response.isSuccessful && response.body() != null) {
            val entities = response.body()!!.map { MarketIndexEntity(it.index ?: it.name ?: "", it.name ?: "", it.value ?: 0.0, it.changePercent ?: 0.0) }
            stockDao.insertIndices(entities)
            true
        } else false
    }

    suspend fun addSymbolToWatchlist(symbol: String, fullName: String) = stockDao.insertSymbol(StockSymbolEntity(symbol, fullName, 0.0, 0.0))
    suspend fun removeSymbolFromWatchlist(symbol: String) = stockDao.setWatchlist(symbol, false)

    suspend fun checkAlerts(rates: List<MarketRateEntity>, stocks: List<StockSymbolEntity>): List<PriceAlertEntity> {
        val allAlerts = alerts.first().filter { it.isActive }
        val triggered = mutableListOf<PriceAlertEntity>()
        
        allAlerts.forEach { alert ->
            val currentPriceRial = when {
                rates.any { it.assetCode == alert.assetCode } -> rates.find { it.assetCode == alert.assetCode }!!.priceToman * RIAL_PER_TOMAN
                stocks.any { it.symbol == alert.assetCode } -> stocks.find { it.symbol == alert.assetCode }!!.lastPriceRial
                else -> null
            }
            
            if (currentPriceRial != null) {
                val isTriggered = when (alert.direction) {
                    AlertDirection.ABOVE -> currentPriceRial >= alert.targetPriceRial
                    AlertDirection.BELOW -> currentPriceRial <= alert.targetPriceRial
                }
                
                if (isTriggered) {
                    triggered.add(alert)
                }
            }
        }
        return triggered
    }
}
