package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Encrypted Room Entity for Financial Assets & Currency Market Rates.
 * Indexed by [updatedAt] for fast temporal queries.
 */
@Entity(
    tableName = "market_rates",
    indices = [Index(value = ["updatedAt"])]
)
data class MarketRateEntity(
    @PrimaryKey
    val assetCode: String, // e.g. "USD", "EUR", "GOLD_18K", "AZADI"
    val name: String,
    val priceToman: BigDecimal,
    val priceGlobal: BigDecimal = BigDecimal.ZERO,
    val currency: String = "تومان",
    val changePercent: BigDecimal,
    val updatedAt: Long = System.currentTimeMillis(),
    val isOfflineRate: Boolean = false
)

/**
 * Encrypted Room Entity for Investment Funds NAV & Performance Metrics.
 */
@Entity(
    tableName = "mutual_funds",
    indices = [Index(value = ["id"])]
)
data class MutualFundEntity(
    @PrimaryKey
    val id: String, // e.g. "FARABI", "MOFID", "ETEMAD"
    val name: String,
    val navToman: BigDecimal,
    val returnPercent: BigDecimal, // Monthly/Annual return
    val riskLevel: String, // Low, Medium, High
    val manager: String
)
