package com.example.data.repository

import com.example.data.local.AlertDirection
import com.example.data.local.AssetPurchaseDao
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.AssetSaleDao
import com.example.data.local.AssetSaleEntity
import com.example.data.local.MarketDao
import com.example.data.local.MarketIndexEntity
import com.example.data.local.MarketRateEntity
import com.example.data.local.PortfolioAssetType
import com.example.data.local.PriceAlertDao
import com.example.data.local.PriceAlertEntity
import com.example.data.local.StockDao
import com.example.data.local.StockSymbolEntity
import com.example.data.remote.MarketApiService
import com.example.data.remote.TsetmcApiClient
import com.example.data.remote.TsetmcApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** One row of the "خانه" / "افزودن خرید" holdings summary: everything the user owns of one asset. */
data class HoldingSummary(
    val assetType: PortfolioAssetType,
    val assetCode: String,
    val assetName: String,
    val quantity: Double,
    val totalPaidRial: Double,
    val currentPriceRial: Double,
    val currentValueRial: Double,
    val profitLossRial: Double,
    val profitLossPercent: Double
)

/**
 * All monetary output of this repository is in RIAL. Internally the gold/currency endpoint
 * reports Toman (see MarketApiService), so it is multiplied by 10 here at the boundary —
 * RIAL_PER_TOMAN — and nowhere else in the portfolio module.
 *
 * Market data is fetched DIRECTLY from BrsApi.ir using [apiKey] — no proxy. A Cloudflare
 * Worker proxy was tried first to keep the key out of the APK, but BrsApi.ir rejected
 * requests coming from the Worker's IPs with 401 Unauthorized, so the proxy was dropped.
 * This means [apiKey] ends up compiled into the APK's BuildConfig and is extractable via
 * decompilation (apktool/jadx) — see README.md. If [apiKey] is blank, live refreshes simply
 * no-op (offline banner).
 */
class PortfolioRepository(
    private val purchaseDao: AssetPurchaseDao,
    private val saleDao: AssetSaleDao,
    private val marketDao: MarketDao,
    private val stockDao: StockDao,
    private val alertDao: PriceAlertDao,
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
    val watchlist: Flow<List<StockSymbolEntity>> = stockDao.getWatchlist()
    val indices: Flow<List<MarketIndexEntity>> = stockDao.getIndices()
    val alerts: Flow<List<PriceAlertEntity>> = alertDao.getAllAlerts()

    /** Sum of realized profit/loss across every sale ever recorded — the "سود محقق‌شده" figure. */
    val totalRealizedPnlRial: Flow<Double> = sales.map { list -> list.sumOf { it.realizedPnlRial } }

    /**
     * Combines purchases MINUS sold quantity/cost-basis + latest market rates into a
     * per-asset holdings summary, all in Rial. A holding disappears once its net quantity
     * reaches ~0 (fully sold) rather than showing a zero/negative row.
     */
    val holdings: Flow<List<HoldingSummary>> = combine(purchases, sales, marketRates, watchlist) { txns, soldTxns, rates, stocks ->
        val soldByCode = soldTxns.groupBy { it.assetCode }
        txns.groupBy { it.assetCode }.mapNotNull { (code, group) ->
            val type = group.first().assetType
            val purchasedQty = group.sumOf { it.quantity }
            val purchasedCost = group.sumOf { it.totalPaidRial }
            val soldQty = soldByCode[code]?.sumOf { it.quantitySold } ?: 0.0
            val soldCostBasis = soldByCode[code]?.sumOf { it.costBasisRial } ?: 0.0

            val quantity = purchasedQty - soldQty
            val totalPaid = purchasedCost - soldCostBasis
            if (quantity <= 0.0001) return@mapNotNull null

            val currentPriceRial = when (type) {
                PortfolioAssetType.STOCK -> stocks.find { it.symbol == code }?.lastPriceRial
                else -> rates.find { it.assetCode == code }?.let { it.priceToman * RIAL_PER_TOMAN }
            } ?: (totalPaid / quantity.coerceAtLeast(0.0001)) // fallback: no live price yet, use cost basis
            val currentValue = quantity * currentPriceRial
            val pnl = currentValue - totalPaid
            HoldingSummary(
                assetType = type,
                assetCode = code,
                assetName = group.first().assetName,
                quantity = quantity,
                totalPaidRial = totalPaid,
                currentPriceRial = currentPriceRial,
                currentValueRial = currentValue,
                profitLossRial = pnl,
                profitLossPercent = if (totalPaid > 0) (pnl / totalPaid) * 100.0 else 0.0
            )
        }
    }

    suspend fun addPurchase(purchase: AssetPurchaseEntity) {
        require(purchase.quantity > 0.0) { "مقدار خرید باید بیشتر از صفر باشد" }
        require(purchase.unitPriceRial > 0.0) { "قیمت خرید باید بیشتر از صفر باشد" }
        require(purchase.totalPaidRial > 0.0) { "مبلغ خرید باید بیشتر از صفر باشد" }
        purchaseDao.insertPurchase(purchase.copy(totalPaidRial = purchase.quantity * purchase.unitPriceRial))
    }

    suspend fun deletePurchase(id: Long) {
        val purchase = purchases.first().firstOrNull { it.id == id }
            ?: return
        val soldForAsset = sales.first()
            .filter { it.assetCode == purchase.assetCode && it.assetType == purchase.assetType }
        require(soldForAsset.isEmpty()) {
            "این خرید قبلاً در محاسبه فروش استفاده شده و حذف آن باعث تغییر سود محقق‌شده می‌شود"
        }
        purchaseDao.deletePurchase(id)
    }

    /**
     * Records a sale of [quantitySold] units of [assetCode] at [saleUnitPriceRial]. Cost
     * basis is the current average unit cost across that asset's (unsold) purchases — throws
     * if trying to sell more than currently held, so the person can't create a negative holding.
     */
    suspend fun sellAsset(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantitySold: Double,
        saleUnitPriceRial: Double,
        saleDate: Long = System.currentTimeMillis()
    ): AssetSaleEntity {
        require(quantitySold > 0.0) { "مقدار فروش باید بیشتر از صفر باشد" }
        require(saleUnitPriceRial > 0.0) { "قیمت فروش باید بیشتر از صفر باشد" }

        val allPurchases = purchases.first().filter {
            it.assetCode == assetCode && it.assetType == assetType
        }
        val allSales = sales.first().filter {
            it.assetCode == assetCode && it.assetType == assetType
        }
        require(allPurchases.isNotEmpty()) { "این دارایی در پرتفوی وجود ندارد" }

        val purchasedQty = allPurchases.sumOf { it.quantity }
        val purchasedCost = allPurchases.sumOf { it.totalPaidRial }
        val alreadySoldQty = allSales.sumOf { it.quantitySold }
        val alreadySoldCost = allSales.sumOf { it.costBasisRial }

        val remainingQty = purchasedQty - alreadySoldQty
        val remainingCost = purchasedCost - alreadySoldCost
        require(remainingQty > 0.0001) { "موجودی این دارایی صفر است" }
        require(quantitySold <= remainingQty + 0.0001) { "بیشتر از مقدار موجود نمی‌توانی بفروشی" }

        val avgUnitCost = if (remainingQty > 0) remainingCost / remainingQty else 0.0
        val costBasis = avgUnitCost * quantitySold
        val totalReceived = quantitySold * saleUnitPriceRial

        val sale = AssetSaleEntity(
            assetType = assetType,
            assetCode = assetCode,
            assetName = assetName,
            quantitySold = quantitySold,
            saleUnitPriceRial = saleUnitPriceRial,
            totalReceivedRial = totalReceived,
            costBasisRial = costBasis,
            realizedPnlRial = totalReceived - costBasis,
            saleDate = saleDate
        )
        saleDao.insertSale(sale)
        return sale
    }

    suspend fun deleteSale(id: Long) = saleDao.deleteSale(id)

    suspend fun addAlert(alert: PriceAlertEntity) = alertDao.insertAlert(alert)
    suspend fun deleteAlert(id: Long) = alertDao.deleteAlert(id)

    /** Fetches live gold/USD rates. Returns true on a successful live fetch, false if offline fallback used. */
    suspend fun refreshGoldAndDollar(): Boolean {
        val service = marketApiService ?: return false
        return try {
            val response = service.getGoldCurrency(apiKey)
            val body = response.body()
            if (response.isSuccessful && body != null && body.successful != false) {
                val liveRates = (body.gold + body.currency)
                    .filter { it.unit == "تومان" }
                    .map { dto ->
                        MarketRateEntity(
                            assetCode = dto.symbol,
                            name = dto.name,
                            priceToman = dto.price,
                            changePercent = dto.changePercent,
                            isOfflineRate = false
                        )
                    }
                if (liveRates.isNotEmpty()) marketDao.insertMarketRates(liveRates)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /** Fetches ALL bourse symbols in one call and updates only the ones in the user's watchlist. */
    suspend fun refreshWatchlist(symbols: List<String>): Boolean {
        val service = tsetmcApiService
        if (service == null || symbols.isEmpty()) return false
        return try {
            val response = service.getAllSymbols()
            val body = response.body()
            if (!response.isSuccessful || body == null) return false

            var anySuccess = false
            for (wanted in symbols) {
                val match = body.find { it.symbol == wanted } ?: continue
                if (!match.hasValidPrice) continue
                stockDao.insertSymbol(
                    StockSymbolEntity(
                        symbol = match.symbol ?: wanted,
                        fullName = match.fullName ?: wanted,
                        // NOTE: unlike Gold_Currency (Toman), Tsetmc's AllSymbols already
                        // reports prices in Rial (verified via P/E ratio) — no ×10 here.
                        lastPriceRial = match.closingPrice ?: 0.0,
                        changePercent = match.changePercent ?: 0.0,
                        buyPriceRial = match.buyPrice ?: 0.0,
                        sellPriceRial = match.sellPrice ?: 0.0,
                        isInWatchlist = true
                    )
                )
                anySuccess = true
            }
            anySuccess
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addSymbolToWatchlist(symbol: String, fullName: String) {
        stockDao.insertSymbol(
            StockSymbolEntity(symbol = symbol, fullName = fullName, lastPriceRial = 0.0, changePercent = 0.0)
        )
    }

    /** Removes a symbol from the watchlist (keeps the cached row, just stops showing/refreshing it). */
    suspend fun removeSymbolFromWatchlist(symbol: String) {
        stockDao.setWatchlist(symbol, false)
    }

    /** Fetches شاخص کل / شاخص هم‌وزن. Returns true on a successful live fetch. */
    suspend fun refreshIndices(): Boolean {
        val service = tsetmcApiService ?: return false
        return try {
            val response = service.getIndices()
            val body = response.body()
            if (response.isSuccessful && body != null && body.isNotEmpty()) {
                val entities = body.mapNotNull { dto ->
                    val value = dto.value ?: return@mapNotNull null
                    MarketIndexEntity(
                        indexCode = dto.index ?: dto.name ?: "INDEX",
                        name = dto.name ?: "شاخص کل",
                        value = value,
                        changePercent = dto.changePercent ?: 0.0
                    )
                }
                if (entities.isNotEmpty()) stockDao.insertIndices(entities)
                entities.isNotEmpty()
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /** Checks all active alerts against the latest known rates/stocks; returns triggered ones and marks them. */
    suspend fun checkAlerts(rates: List<MarketRateEntity>, stocks: List<StockSymbolEntity>): List<PriceAlertEntity> {
        val active = alertDao.getActiveAlerts()
        val triggered = mutableListOf<PriceAlertEntity>()
        for (alert in active) {
            val currentPrice = rates.find { it.assetCode == alert.assetCode }?.let { it.priceToman * RIAL_PER_TOMAN }
                ?: stocks.find { it.symbol == alert.assetCode }?.lastPriceRial
                ?: continue
            val hit = when (alert.direction) {
                AlertDirection.ABOVE -> currentPrice >= alert.targetPriceRial
                AlertDirection.BELOW -> currentPrice <= alert.targetPriceRial
            }
            if (hit) {
                alertDao.markTriggered(alert.id, System.currentTimeMillis())
                triggered.add(alert)
            }
        }
        return triggered
    }
}
