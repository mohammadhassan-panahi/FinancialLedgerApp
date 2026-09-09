package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "portfolio_snapshots")
data class PortfolioSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalValueRial: BigDecimal,
    val totalProfitLossRial: BigDecimal,
    val goldPriceRial: BigDecimal,
    val usdPriceRial: BigDecimal,
    val stockIndexValue: BigDecimal,
    val allocationByAssetJson: String, // Map<String, Double> serialized to JSON
    val allocationByTypeJson: String   // Map<PortfolioAssetType, Double> serialized to JSON
)
