package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Historical snapshot of the portfolio value and benchmarks.
 * Captured once or twice a day to build performance charts.
 */
@Entity(tableName = "portfolio_snapshots")
data class PortfolioSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalValueRial: Double,
    val totalProfitLossRial: Double,
    val goldPriceRial: Double,
    val usdPriceRial: Double,
    val stockIndexValue: Double = 0.0,
    // JSON strings for complex allocations to keep it flat
    val allocationByAssetJson: String = "",
    val allocationByTypeJson: String = ""
)
