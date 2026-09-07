package com.example.domain.model

import androidx.compose.runtime.Immutable

/**
 * Summary of the entire portfolio.
 * Used for the Hero Card on the main dashboard.
 */
@Immutable
data class PortfolioSummary(
    val totalValueRial: Double,
    val totalProfitLossRial: Double,
    val totalProfitLossPercent: Double,
    val todayProfitLossRial: Double,
    val todayProfitLossPercent: Double,
    val lastUpdated: Long,
    val marketStatus: String,
    val usdRateRial: Double,
    val gold18kPriceRial: Double,
    val bestPerformer: Holding? = null,
    val worstPerformer: Holding? = null,
    val allocationByAsset: List<AllocationItem> = emptyList(),
    val allocationByType: List<AllocationItem> = emptyList(),
    val goldAnalysis: GoldPriceAnalysis? = null,
    val insights: List<String> = emptyList()
)

data class GoldPriceAnalysis(
    val globalGoldChangePercent: Double,
    val usdChangePercent: Double,
    val localGoldChangePercent: Double,
    val primaryDriver: String
)

data class AllocationItem(
    val label: String,
    val percentage: Double,
    val valueRial: Double
)
