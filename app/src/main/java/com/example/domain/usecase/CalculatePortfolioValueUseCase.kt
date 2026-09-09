package com.example.domain.usecase

import com.example.data.local.PortfolioAssetType
import com.example.data.repository.PortfolioRepository
import com.example.util.safeDiv
import com.example.util.sumOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal

data class PortfolioSummary(
    val totalRial: BigDecimal,
    val totalUsdt: BigDecimal,
    val assetBreakdown: Map<PortfolioAssetType, BigDecimal>,
    val dailyChangeRial: BigDecimal,
    val dailyChangePercent: BigDecimal
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
            val usdRateToman = rates.find { it.assetCode == "USD" }?.priceToman ?: BigDecimal("60000")
            val usdToRial = usdRateToman.multiply(BigDecimal("10"))

            val totalRial = holdings.sumOf { it.currentValueRial }
            val totalUsdt = totalRial.safeDiv(usdToRial)
            
            val breakdown = holdings.groupBy { it.assetType }
                .mapValues { (_, group) -> group.sumOf { it.currentValueRial } }
            
            val totalDailyChangeRial = holdings.sumOf { it.dailyChangeRial }
            val prevValueRial = totalRial.subtract(totalDailyChangeRial)
            val totalDailyChangePercent = if (prevValueRial.compareTo(BigDecimal.ZERO) > 0) totalDailyChangeRial.safeDiv(prevValueRial).multiply(BigDecimal("100")) else BigDecimal.ZERO

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
