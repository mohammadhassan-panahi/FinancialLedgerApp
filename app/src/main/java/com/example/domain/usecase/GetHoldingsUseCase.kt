package com.example.domain.usecase

import com.example.data.repository.PortfolioRepository
import com.example.domain.model.Holding
import kotlinx.coroutines.flow.Flow

class GetHoldingsUseCase(private val repository: PortfolioRepository) {
    operator fun invoke(): Flow<List<Holding>> = repository.holdings
}
