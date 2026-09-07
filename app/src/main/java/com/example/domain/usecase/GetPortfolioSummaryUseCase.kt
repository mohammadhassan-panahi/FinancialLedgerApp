package com.example.domain.usecase

import com.example.data.repository.PortfolioRepository
import com.example.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.Flow

class GetPortfolioSummaryUseCase(private val repository: PortfolioRepository) {
    operator fun invoke(): Flow<PortfolioSummary> = repository.portfolioSummary
}
