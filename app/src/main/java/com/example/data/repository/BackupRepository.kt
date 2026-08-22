package com.example.data.repository

import com.example.data.local.AssetPurchaseDao
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.AssetSaleDao
import com.example.data.local.AssetSaleEntity
import com.example.data.local.PriceAlertDao
import com.example.data.local.PriceAlertEntity
import com.example.data.local.StockDao
import com.example.data.local.StockSymbolEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first

/**
 * Exports/imports the user's own data (purchases, sales, watchlist, alerts) as a single JSON
 * file — NOT market rates, which are always re-fetched live and would just go stale in a
 * backup. The format is intentionally readable; users should treat exported files as sensitive
 * financial data and store them only in trusted/private locations. Import is atomic and validated.
 */
@JsonClass(generateAdapter = true)
data class BackupPayload(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val purchases: List<AssetPurchaseEntity>,
    val sales: List<AssetSaleEntity> = emptyList(),
    val watchlist: List<StockSymbolEntity>,
    val alerts: List<PriceAlertEntity>
)

class BackupRepository(
    private val purchaseDao: AssetPurchaseDao,
    private val saleDao: AssetSaleDao,
    private val stockDao: StockDao,
    private val alertDao: PriceAlertDao,
    private val database: AppDatabase
) {
    private val moshi = Moshi.Builder().build()

    @OptIn(kotlin.ExperimentalStdlibApi::class)
    suspend fun exportToJson(): String {
        val payload = BackupPayload(
            purchases = purchaseDao.getAllPurchases().first(),
            sales = saleDao.getAllSales().first(),
            watchlist = stockDao.getWatchlistOnce(),
            alerts = alertDao.getAllAlerts().first()
        )
        return moshi.adapter<BackupPayload>().indent("  ").toJson(payload)
    }

    /** Returns the number of items imported, or throws on invalid/corrupt JSON. */
    @OptIn(kotlin.ExperimentalStdlibApi::class)
    suspend fun importFromJson(json: String): Int {
        val payload = moshi.adapter<BackupPayload>().fromJson(json)
            ?: throw IllegalArgumentException("فایل پشتیبان نامعتبر است")

        require(payload.version in 1..2) { "نسخه پشتیبان پشتیبانی نمی‌شود" }
        require(payload.purchases.all { it.quantity > 0.0 && it.unitPriceRial > 0.0 }) {
            "فایل پشتیبان شامل خرید نامعتبر است"
        }
        require(payload.sales.all { it.quantitySold > 0.0 && it.saleUnitPriceRial > 0.0 && it.costBasisRial >= 0.0 }) {
            "فایل پشتیبان شامل فروش نامعتبر است"
        }
        require(payload.watchlist.all { it.symbol.isNotBlank() }) { "نماد نامعتبر است" }
        require(payload.alerts.all { it.assetCode.isNotBlank() && it.targetPriceRial > 0.0 }) {
            "هشدار قیمت نامعتبر است"
        }

        database.withTransaction {
            payload.purchases.forEach { purchaseDao.insertPurchase(it) }
            payload.sales.forEach { saleDao.insertSale(it) }
            payload.watchlist.forEach { stockDao.insertSymbol(it) }
            payload.alerts.forEach { alertDao.insertAlert(it) }
        }

        return payload.purchases.size + payload.sales.size + payload.watchlist.size + payload.alerts.size
    }
}
