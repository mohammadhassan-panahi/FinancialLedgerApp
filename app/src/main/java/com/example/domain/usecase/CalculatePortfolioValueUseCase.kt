package com.example.domain.usecase

import com.example.data.local.PortfolioAssetType
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class PortfolioSummary(
    val totalRial: Double,
    val totalUsdt: Double,
    val assetBreakdown: Map<PortfolioAssetType, Double>,
    val dailyChangeRial: Double,
    val dailyChangePercent: Double
)

/**
 * Aggregates values from all asset types including Gold, USD, Stocks, Crypto, 
 * and Vehicles/Real Estate to calculate total portfolio value.
 */
class CalculatePortfolioValueUseCase(private val repository: PortfolioRepository) {
    operator fun invoke(): Flow<PortfolioSummary> {
        return combine(
            repository.holdings,
            repository.marketRates
        ) { holdings, rates ->
            val usdRateToman = rates.find { it.assetCode == "USD" }?.priceToman ?: 60000.0
            val usdToRial = usdRateToman * 10.0

            val totalRial = holdings.sumOf { it.currentValueRial }
            val totalUsdt = totalRial / usdToRial
            
            val breakdown = holdings.groupBy { it.assetType }
                .mapValues { (_, group) -> group.sumOf { it.currentValueRial } }
            
            val totalDailyChangeRial = holdings.sumOf { it.dailyChangeRial }
            val prevValueRial = totalRial - totalDailyChangeRial
            val totalDailyChangePercent = if (prevValueRial > 0) (totalDailyChangeRial / prevValueRial) * 100.0 else 0.0

            PortfolioSummary(
                totalRial = totalRial,
                totalUsdt = totalUsdt,
                assetBreakdown = breakdown,
                dailyChangeRial = totalDailyChangeRial,
                dailyChangePercent = totalDailyChangePercent
            )
        }
    }
}
