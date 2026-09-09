package com.example.domain.model

import java.math.BigDecimal

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
    val price: BigDecimal,          // Base unit depends on type (usually Rial/Toman)
    val previousPrice: BigDecimal,
    val change: BigDecimal,
    val changePercent: BigDecimal,
    val timestamp: Long,
    val source: String
)
