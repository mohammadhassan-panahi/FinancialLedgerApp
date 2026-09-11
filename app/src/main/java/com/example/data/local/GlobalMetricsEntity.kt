package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Global cryptocurrency market statistics.
 * Stored as a single row in the database for offline access.
 */
@Entity(tableName = "global_market_metrics")
data class GlobalMetricsEntity(
    @PrimaryKey
    val id: Int = 1, // We only ever need one row of current stats
    val totalMarketCapUsd: BigDecimal?,
    val totalVolume24hUsd: BigDecimal?,
    val btcDominance: BigDecimal?,
    val ethDominance: BigDecimal?,
    val activeCryptocurrencies: Int?,
    val fearAndGreedValue: Int? = null,
    val fearAndGreedLabel: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
