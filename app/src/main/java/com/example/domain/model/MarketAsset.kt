package com.example.domain.model

enum class MarketAssetType {
    GOLD, COIN, CURRENCY, STOCK, COMMODITY, CRYPTO, CASH
}

/**
 * Standardized market asset model for the application.
 * Decouples the UI from raw API DTOs.
 */
data class MarketAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val type: MarketAssetType,
    val price: Double,          // Base unit depends on type (usually Rial/Toman)
    val previousPrice: Double,
    val change: Double,
    val changePercent: Double,
    val timestamp: Long,
    val source: String
)
