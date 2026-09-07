package com.example.domain.model

import androidx.compose.runtime.Immutable
import com.example.data.local.PortfolioAssetType

/**
 * Domain model representing a single asset holding in the portfolio.
 */
@Immutable
data class Holding(
    val assetType: PortfolioAssetType,
    val assetCode: String,
    val assetName: String,
    val quantity: Double,
    val totalPaidRial: Double,
    val currentPriceRial: Double,
    val currentValueRial: Double,
    val profitLossRial: Double,
    val profitLossPercent: Double,
    val dailyChangePercent: Double = 0.0,
    val dailyChangeRial: Double = 0.0,
    val inflationAdjustedProfitLossRial: Double = 0.0,
    val cmcId: Int? = null
)
