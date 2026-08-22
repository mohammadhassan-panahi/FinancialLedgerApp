package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val priceToman: Double,
    val changePercent: Double,
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
    val navToman: Double,
    val returnPercent: Double, // Monthly/Annual return
    val riskLevel: String, // Low, Medium, High
    val manager: String
)
