package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Portfolio module entities. ALL monetary fields across this module are stored in RIAL
 * (base currency for the whole app, per product requirement) — never Toman here, to avoid
 * mixing units with the legacy ledger tables (MarketRateEntity.priceToman, TransactionEntity)
 * that predate this module. Use PersianNumberUtils.formatRial() to display these values.
 */
enum class PortfolioAssetType { GOLD, USD, STOCK }

/**
 * A single "خرید" (purchase) the user logged for something they own: grams of gold, USD
 * amount, or a number of shares of a Tehran Stock Exchange symbol.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "asset_purchases", indices = [Index(value = ["assetCode"])])
data class AssetPurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetType: PortfolioAssetType,
    val assetCode: String,      // e.g. "GOLD_18K", "USD", or a TSE symbol like "فولاد"
    val assetName: String,      // display name at time of purchase
    val quantity: Double,       // grams (gold), USD units (dollar), or share count (stock)
    val unitPriceRial: Double,  // price per unit paid, in Rial
    val totalPaidRial: Double,  // quantity * unitPriceRial (kept denormalized for fast sums)
    val purchaseDate: Long = System.currentTimeMillis(),
    val note: String = ""
)

/**
 * A "فروش" (sale) of some quantity of an asset the user already holds. Cost basis is
 * computed at sale time from the average unit cost of that assetCode's purchases so far —
 * simple average-cost accounting rather than FIFO/LIFO lot matching, which keeps the mental
 * model (and the UI) simple: "you sold X units at Y price, here's the profit."
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "asset_sales", indices = [Index(value = ["assetCode"])])
data class AssetSaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetType: PortfolioAssetType,
    val assetCode: String,
    val assetName: String,
    val quantitySold: Double,
    val saleUnitPriceRial: Double,
    val totalReceivedRial: Double,   // quantitySold * saleUnitPriceRial
    val costBasisRial: Double,       // average cost of quantitySold, at time of sale
    val realizedPnlRial: Double,     // totalReceivedRial - costBasisRial
    val saleDate: Long = System.currentTimeMillis(),
    val note: String = ""
)

/** Cached Tehran Stock Exchange (TSETMC) index values, e.g. شاخص کل / شاخص هم‌وزن. */
@Entity(tableName = "market_index")
data class MarketIndexEntity(
    @PrimaryKey
    val indexCode: String, // "TOTAL_INDEX" | "EQUAL_WEIGHT"
    val name: String,
    val value: Double,
    val changePercent: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

/** Cached/watch-listed Tehran Stock Exchange symbols (includes stocks, ETFs, and funds). */
@JsonClass(generateAdapter = true)
@Entity(tableName = "stock_symbols")
data class StockSymbolEntity(
    @PrimaryKey
    val symbol: String, // e.g. "فولاد", "خودرو", or an ETF/fund symbol
    val fullName: String,
    val lastPriceRial: Double,
    val changePercent: Double,
    val buyPriceRial: Double = 0.0,   // بهترین قیمت خرید (صف خرید) — ۰ یعنی هنوز دریافت نشده
    val sellPriceRial: Double = 0.0,  // بهترین قیمت فروش (صف فروش) — ۰ یعنی هنوز دریافت نشده
    val isInWatchlist: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AlertDirection { ABOVE, BELOW }

/** User-defined price alerts, checked periodically by PriceAlertWorker. */
@JsonClass(generateAdapter = true)
@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetCode: String,
    val assetName: String,
    val targetPriceRial: Double,
    val direction: AlertDirection,
    val isActive: Boolean = true,
    val lastTriggeredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
