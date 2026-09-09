package com.example.domain.model

import androidx.compose.runtime.Immutable
import com.example.data.local.PortfolioAssetType
import java.math.BigDecimal

/**
 * Domain model representing a single asset holding in the portfolio.
 */
@Immutable
data class Holding(
    val assetType: PortfolioAssetType,
    val assetCode: String,
    val assetName: String,
    val quantity: BigDecimal,
    val totalPaidRial: BigDecimal,
    val currentPriceRial: BigDecimal,
    val currentValueRial: BigDecimal,
    val profitLossRial: BigDecimal,
    val profitLossPercent: BigDecimal,
    val dailyChangePercent: BigDecimal = BigDecimal.ZERO,
    val dailyChangeRial: BigDecimal = BigDecimal.ZERO,
    val inflationAdjustedProfitLossRial: BigDecimal = BigDecimal.ZERO,
    val cmcId: Int? = null
)
