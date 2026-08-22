package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached market data for one cryptocurrency, sourced from CoinMarketCap's free/Basic tier
 * (/listings/latest and /quotes/latest — see CoinMarketCapApiService). Only fields available
 * on the free tier are stored here; everything else in the originally requested feature list
 * (Holder Concentration, Security/Audit, Developer Activity, on-chain Network stats,
 * Ecosystem/TVL, Technical Analysis indicators) is out of scope for this table and will need
 * separate entities/data sources if/when those integrations are added.
 *
 * All prices are stored in USD (CoinMarketCap's native currency for this app's `convert`
 * param) — Toman/Rial display conversion happens in the UI layer using the app's existing
 * USD/Toman rate from MarketRateEntity, exactly like the rest of the portfolio module keeps
 * conversion at the boundary rather than mixing units in storage.
 */
@Entity(tableName = "crypto_assets", indices = [Index(value = ["symbol"], unique = true)])
data class CryptoAssetEntity(
    @PrimaryKey
    val cmcId: Int,                     // CoinMarketCap's numeric id — stable, unlike symbol
    val symbol: String,                 // e.g. "BTC"
    val name: String,                   // e.g. "Bitcoin"
    val slug: String? = null,
    val cmcRank: Int? = null,
    val priceUsd: Double? = null,
    val percentChange1h: Double? = null,
    val percentChange24h: Double? = null,
    val percentChange7d: Double? = null,
    val percentChange30d: Double? = null,
    val marketCapUsd: Double? = null,
    val fullyDilutedMarketCapUsd: Double? = null,
    val volume24hUsd: Double? = null,
    val volumeChange24h: Double? = null,
    val circulatingSupply: Double? = null,
    val totalSupply: Double? = null,
    val maxSupply: Double? = null,
    val infiniteSupply: Boolean = false,
    val platformName: String? = null,   // e.g. "Ethereum" for an ERC-20 token; null for native coins
    val tokenAddress: String? = null,
    val tags: String? = null,           // comma-joined; SQLite has no native array/list column
    val isInWatchlist: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis() // when THIS APP fetched it, for staleness checks
)

/**
 * Static metadata for a coin (website, whitepaper, logo, description) from
 * /cryptocurrency/info. Kept in a separate table from CryptoAssetEntity because this data
 * changes rarely and should be cached much longer / refreshed far less often than live
 * market data, to conserve CMC's free-tier call credits.
 */
@Entity(tableName = "crypto_info")
data class CryptoInfoEntity(
    @PrimaryKey
    val cmcId: Int,
    val category: String? = null,       // "coin" or "token"
    val description: String? = null,
    val logoUrl: String? = null,
    val websiteUrl: String? = null,
    val whitepaperUrl: String? = null,
    val explorerUrl: String? = null,
    val sourceCodeUrl: String? = null,  // GitHub — reused later for Developer Activity if that's added
    val dateAdded: String? = null,      // ISO date string as returned by CMC
    val lastUpdated: Long = System.currentTimeMillis()
)
