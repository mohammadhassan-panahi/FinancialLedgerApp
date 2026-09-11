package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.MarketApiService
import com.example.data.remote.TsetmcApiClient
import com.example.data.remote.TsetmcApiService
import com.example.domain.model.AllocationItem
import com.example.domain.model.GoldPriceAnalysis
import com.example.domain.model.Holding
import com.example.domain.model.PortfolioSummary
import com.example.util.BigDecimalAdapter
import com.example.util.safeDiv
import com.example.util.sumOf
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode

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
        val RIAL_PER_TOMAN = BigDecimal("10")
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

    val totalDebtRial: Flow<BigDecimal> = debtCreditDao.getTotalDebtFlow().map { it.sumOf { it } }
    val totalCreditRial: Flow<BigDecimal> = debtCreditDao.getTotalCreditFlow().map { it.sumOf { it } }
    val totalRealizedPnlRial: Flow<BigDecimal> = sales.map { list -> list.sumOf { it.realizedPnlRial } }
    val totalLiquidityRial: Flow<BigDecimal> = bankAccountDao.getAllBalances().map { list -> 
        list.sumOf { it }.multiply(RIAL_PER_TOMAN) 
    }

    /**
     * Combines all data sources to provide a unified view of the user's portfolio.
     */
    val holdings: Flow<List<Holding>> = combine(
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
        val liquidityRial = array[6] as BigDecimal
        val vehicleRates = array[7] as List<VehicleEntity>
        val propertyRates = array[8] as List<RealEstateEntity>
        
        val result = mutableListOf<Holding>()
        val usdRateToman = rates.find { it.assetCode == "USD" }?.priceToman ?: BigDecimal("60000")
        val usdToRial = usdRateToman.multiply(RIAL_PER_TOMAN)

        if (liquidityRial.compareTo(BigDecimal.ZERO) > 0) {
            result.add(Holding(PortfolioAssetType.CASH, "CASH_RIAL", "نقدینگی", liquidityRial, liquidityRial, BigDecimal.ONE, liquidityRial, BigDecimal.ZERO, BigDecimal.ZERO))
        }

        val soldByCode = soldTxns.groupBy { it.assetCode }
        txns.groupBy { it.assetCode }.forEach { (code, group) ->
            val type = group.first().assetType
            val purchasedQty = group.sumOf { it.quantity }
            val purchasedCost = group.sumOf { it.totalPaidRial }
            val soldQty = soldByCode[code]?.sumOf { it.quantitySold } ?: BigDecimal.ZERO
            val soldCostBasis = soldByCode[code]?.sumOf { it.costBasisRial } ?: BigDecimal.ZERO

            val quantity = purchasedQty.subtract(soldQty)
            val totalPaid = purchasedCost.subtract(soldCostBasis)
            if (quantity.compareTo(BigDecimal("0.0001")) <= 0) return@forEach

            val currentPriceRial: BigDecimal
            val dailyChangePercent: BigDecimal
            
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
                    currentPriceRial = (cryptoMatch.priceUsd ?: BigDecimal.ZERO).multiply(usdToRial)
                    dailyChangePercent = cryptoMatch.percentChange24h ?: BigDecimal.ZERO
                }
                type == PortfolioAssetType.FUND && fundMatch != null -> {
                    currentPriceRial = fundMatch.navToman.multiply(RIAL_PER_TOMAN)
                    dailyChangePercent = fundMatch.returnPercent.safeDiv(BigDecimal("30"))
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
                    currentPriceRial = rateMatch.priceToman.multiply(RIAL_PER_TOMAN)
                    dailyChangePercent = rateMatch.changePercent
                }
                else -> {
                    currentPriceRial = if (quantity.compareTo(BigDecimal.ZERO) > 0) totalPaid.safeDiv(quantity) else BigDecimal.ZERO
                    dailyChangePercent = BigDecimal.ZERO
                }
            }

            val currentValue = quantity.multiply(currentPriceRial)
            val pnl = currentValue.subtract(totalPaid)
            val dailyChangeRial = if (dailyChangePercent.compareTo(BigDecimal.ZERO) != 0) {
                 val factor = BigDecimal.ONE.subtract(BigDecimal.ONE.safeDiv(BigDecimal.ONE.add(dailyChangePercent.safeDiv(BigDecimal("100")))))
                 currentValue.multiply(factor)
            } else BigDecimal.ZERO
            
            // Assume 40% annual inflation for "Real Growth" calculation (~2.8% monthly)
            val inflationRate = BigDecimal("0.40")
            val purchaseDate = group.first().purchaseDate
            val yearsPassed = (System.currentTimeMillis() - purchaseDate).toDouble() / (1000.0 * 60 * 60 * 24 * 365)
            val inflationFactor = Math.pow(1.0 + inflationRate.toDouble(), yearsPassed).toBigDecimal()
            val inflationAdjustedPaid = totalPaid.multiply(inflationFactor)
            val realPnl = currentValue.subtract(inflationAdjustedPaid)

            result.add(
                Holding(
                    assetType = type,
                    assetCode = code,
                    assetName = group.first().assetName,
                    quantity = quantity,
                    totalPaidRial = totalPaid,
                    currentPriceRial = currentPriceRial,
                    currentValueRial = currentValue,
                    profitLossRial = pnl,
                    profitLossPercent = if (totalPaid.compareTo(BigDecimal.ZERO) > 0) pnl.safeDiv(totalPaid).multiply(BigDecimal("100")) else BigDecimal.ZERO,
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

        val usdRateToman = rates.find { it.assetCode == "USD" }?.priceToman ?: BigDecimal("65000")
        val gold18kRateToman = rates.find { it.assetCode.contains("GOLD_18K") || it.name.contains("۱۸") }?.priceToman ?: BigDecimal("3500000")

        val usdRateRial = usdRateToman.multiply(RIAL_PER_TOMAN)
        val gold18kRateRial = gold18kRateToman.multiply(RIAL_PER_TOMAN)

        val totalPnl = totalValue.subtract(totalPaid)
        val totalPnlPercent = if (totalPaid.compareTo(BigDecimal.ZERO) > 0) totalPnl.safeDiv(totalPaid).multiply(BigDecimal("100")) else BigDecimal.ZERO
        val valueBeforeChange = totalValue.subtract(todayChange)
        val todayPnlPercent = if (valueBeforeChange.compareTo(BigDecimal.ZERO) > 0) todayChange.safeDiv(valueBeforeChange).multiply(BigDecimal("100")) else BigDecimal.ZERO

        val marketStatus = when {
            todayPnlPercent.compareTo(BigDecimal("1.5")) > 0 -> "بازار صعودی قدرتمند"
            todayPnlPercent.compareTo(BigDecimal("0.5")) > 0 -> "بازار مثبت"
            todayPnlPercent.compareTo(BigDecimal("-1.5")) < 0 -> "بازار ریزشی شدید"
            todayPnlPercent.compareTo(BigDecimal("-0.5")) < 0 -> "بازار منفی"
            else -> "بازار متعادل"
        }

        val best = list.filter { it.assetType != PortfolioAssetType.CASH }.maxByOrNull { it.dailyChangePercent }
        val worst = list.filter { it.assetType != PortfolioAssetType.CASH }.minByOrNull { it.dailyChangePercent }

        val byAsset = list.map {
            AllocationItem(it.assetName, if (totalValue.compareTo(BigDecimal.ZERO) > 0) it.currentValueRial.safeDiv(totalValue).multiply(BigDecimal("100")) else BigDecimal.ZERO, it.currentValueRial)
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
                else -> "سایر"
            }
            AllocationItem(label, if (totalValue.compareTo(BigDecimal.ZERO) > 0) typeValue.safeDiv(totalValue).multiply(BigDecimal("100")) else BigDecimal.ZERO, typeValue)
        }.sortedByDescending { it.valueRial }

        val globalGold = rates.find { it.assetCode.contains("XAU") || it.name.contains("انس") }
        val usdRate = rates.find { it.assetCode == "USD" }
        val localGold = rates.find { it.assetCode.contains("GOLD_18K") || it.name.contains("۱۸") }

        val goldAnalysis = if (globalGold != null && usdRate != null && localGold != null) {
            val driver = if (globalGold.changePercent.abs() > usdRate.changePercent.abs()) "GLOBAL_GOLD" else "USD"
            GoldPriceAnalysis(
                globalGoldChangePercent = globalGold.changePercent,
                usdChangePercent = usdRate.changePercent,
                localGoldChangePercent = localGold.changePercent,
                primaryDriver = driver
            )
        } else null

        val insights = mutableListOf<String>()
        best?.let { insights.add("بهترین دارایی امروز شما ${it.assetName} با ${it.dailyChangePercent.setScale(1, RoundingMode.HALF_UP).toPlainString()}٪ رشد بوده است.") }
        byAsset.firstOrNull()?.let { insights.add("بیشترین سهم پورتفوی شما متعلق به ${it.label} است (${it.percentage.setScale(1, RoundingMode.HALF_UP).toPlainString()}٪).") }

        val goldEffect = list.find { it.assetType == PortfolioAssetType.GOLD }?.dailyChangeRial ?: BigDecimal.ZERO
        val totalAbsChange = todayChange.abs()
        if (totalAbsChange.compareTo(BigDecimal.ZERO) > 0 && goldEffect.abs().safeDiv(totalAbsChange).compareTo(BigDecimal("0.5")) > 0) {
            insights.add("تغییرات قیمت طلا بیشترین تأثیر را روی ارزش پورتفوی شما در امروز داشته است.")
        }

        val lastRateUpdate = rates.maxOfOrNull { it.updatedAt } ?: 0L
        val staleHours = (System.currentTimeMillis() - lastRateUpdate) / (1000 * 60 * 60)
        if (staleHours > 3 && lastRateUpdate > 0) {
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
            .add(BigDecimalAdapter())
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        
        // Use specific type to avoid raw list issue
        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, AllocationItem::class.java)
        val adapter = moshi.adapter<List<AllocationItem>>(listType)

        val assetJson = adapter.toJson(summary.allocationByAsset)
        val typeJson = adapter.toJson(summary.allocationByType)

        val snapshot = PortfolioSnapshotEntity(
            totalValueRial = summary.totalValueRial,
            totalProfitLossRial = summary.totalProfitLossRial,
            goldPriceRial = summary.gold18kPriceRial,
            usdPriceRial = summary.usdRateRial,
            stockIndexValue = indices.first().find { it.indexCode == "TOTAL_INDEX" || it.name.contains("کل") }?.value ?: BigDecimal.ZERO,
            allocationByAssetJson = assetJson,
            allocationByTypeJson = typeJson
        )
        snapshotDao.insertSnapshot(snapshot)
    }

    suspend fun addPurchase(purchase: AssetPurchaseEntity) = purchaseDao.insertPurchase(purchase)
    suspend fun deletePurchase(id: Long) = purchaseDao.deletePurchase(id)

    suspend fun sellAsset(assetType: PortfolioAssetType, assetCode: String, assetName: String, quantitySold: BigDecimal, saleUnitPriceRial: BigDecimal, saleDate: Long = System.currentTimeMillis()): AssetSaleEntity {
        val allPurchases = purchases.first().filter { it.assetCode == assetCode && it.assetType == assetType }
        val allSales = sales.first().filter { it.assetCode == assetCode && it.assetType == assetType }
        val remainingQty = allPurchases.sumOf { it.quantity }.subtract(allSales.sumOf { it.quantitySold })
        val remainingCost = allPurchases.sumOf { it.totalPaidRial }.subtract(allSales.sumOf { it.costBasisRial })
        
        require(quantitySold.compareTo(remainingQty.add(BigDecimal("0.0001"))) <= 0) { "موجودی کافی نیست" }
        val costBasis = if (remainingQty.compareTo(BigDecimal.ZERO) > 0) remainingCost.safeDiv(remainingQty).multiply(quantitySold) else BigDecimal.ZERO
        val sale = AssetSaleEntity(
            assetType = assetType,
            assetCode = assetCode,
            assetName = assetName,
            quantitySold = quantitySold,
            saleUnitPriceRial = saleUnitPriceRial,
            totalReceivedRial = quantitySold.multiply(saleUnitPriceRial),
            costBasisRial = costBasis,
            realizedPnlRial = quantitySold.multiply(saleUnitPriceRial).subtract(costBasis),
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
        val response = try { marketApiService?.getGoldCurrency(apiKey) } catch(e: Exception) { null }
        if (response == null || !response.isSuccessful || response.body() == null) return false
        
        val body = response.body()!!
        val liveRates = (body.gold + body.currency).map {
            MarketRateEntity(
                assetCode = it.symbol,
                name = it.name,
                priceToman = if (it.unit == "تومان") it.price.toBigDecimal() else BigDecimal.ZERO,
                priceGlobal = if (it.unit != "تومان") it.price.toBigDecimal() else BigDecimal.ZERO,
                currency = it.unit,
                changePercent = it.changePercent.toBigDecimal(),
                isOfflineRate = false
            )
        }
        if (liveRates.isNotEmpty()) marketDao.insertMarketRates(liveRates)
        return true
    }

    suspend fun refreshWatchlist(symbols: List<String>): Boolean {
        val response = try { tsetmcApiService?.getAllSymbols() } catch(e: Exception) { null }
        if (response == null || !response.isSuccessful || response.body() == null) return false
        
        val body = response.body()!!
        symbols.forEach { sym ->
            body.find { it.symbol == sym }?.let {
                stockDao.insertSymbol(StockSymbolEntity(it.symbol!!, it.fullName!!, (it.closingPrice ?: 0.0).toBigDecimal(), (it.changePercent ?: 0.0).toBigDecimal()))
            }
        }
        return true
    }

    suspend fun refreshIndices(): Boolean {
        val response = try { tsetmcApiService?.getIndices() } catch(e: Exception) { null }
        if (response == null || !response.isSuccessful || response.body() == null) return false
        
        val entities = response.body()!!.map { MarketIndexEntity(it.index ?: it.name ?: "", it.name ?: "", (it.value ?: 0.0).toBigDecimal(), (it.changePercent ?: 0.0).toBigDecimal()) }
        stockDao.insertIndices(entities)
        return true
    }

    suspend fun addSymbolToWatchlist(symbol: String, fullName: String) = stockDao.insertSymbol(StockSymbolEntity(symbol, fullName, BigDecimal.ZERO, BigDecimal.ZERO))
    suspend fun removeSymbolFromWatchlist(symbol: String) = stockDao.setWatchlist(symbol, false)

    suspend fun checkAlerts(rates: List<MarketRateEntity>, stocks: List<StockSymbolEntity>): List<PriceAlertEntity> {
        val allAlerts = alerts.first().filter { it.isActive }
        val triggered = mutableListOf<PriceAlertEntity>()
        
        allAlerts.forEach { alert ->
            val currentPriceRial = when {
                rates.any { it.assetCode == alert.assetCode } -> rates.find { it.assetCode == alert.assetCode }!!.priceToman.multiply(RIAL_PER_TOMAN)
                stocks.any { it.symbol == alert.assetCode } -> stocks.find { it.symbol == alert.assetCode }!!.lastPriceRial
                else -> null
            }
            
            if (currentPriceRial != null) {
                val isTriggered = when (alert.direction) {
                    AlertDirection.ABOVE -> currentPriceRial.compareTo(alert.targetPriceRial) >= 0
                    AlertDirection.BELOW -> currentPriceRial.compareTo(alert.targetPriceRial) <= 0
                }
                
                if (isTriggered) {
                    triggered.add(alert)
                }
            }
        }
        return triggered
    }
}
