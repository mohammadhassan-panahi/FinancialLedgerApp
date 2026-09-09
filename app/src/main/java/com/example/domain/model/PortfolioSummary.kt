package com.example.domain.model

import androidx.compose.runtime.Immutable
import java.math.BigDecimal

/**
 * Summary of the entire portfolio.
 * Used for the Hero Card on the main dashboard.
 */
@Immutable
data class PortfolioSummary(
    val totalValueRial: BigDecimal,
    val totalProfitLossRial: BigDecimal,
    val totalProfitLossPercent: BigDecimal,
    val todayProfitLossRial: BigDecimal,
    val todayProfitLossPercent: BigDecimal,
    val lastUpdated: Long,
    val marketStatus: String,
    val usdRateRial: BigDecimal,
    val gold18kPriceRial: BigDecimal,
    val bestPerformer: Holding? = null,
    val worstPerformer: Holding? = null,
    val allocationByAsset: List<AllocationItem> = emptyList(),
    val allocationByType: List<AllocationItem> = emptyList(),
    val goldAnalysis: GoldPriceAnalysis? = null,
    val insights: List<String> = emptyList()
)

data class GoldPriceAnalysis(
    val globalGoldChangePercent: BigDecimal,
    val usdChangePercent: BigDecimal,
    val localGoldChangePercent: BigDecimal,
    val primaryDriver: String
)

data class AllocationItem(
    val label: String,
    val percentage: BigDecimal,
    val valueRial: BigDecimal
)
