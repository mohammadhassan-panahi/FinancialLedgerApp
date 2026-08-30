package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NexFinRepository(
    private val nexFinDao: NexFinDao,
    private val aiRepository: AiRepository
) {
    // Risk Profile
    val riskProfile: Flow<RiskProfileEntity?> = nexFinDao.getRiskProfile()

    suspend fun saveRiskProfile(score: Int, personality: String) {
        nexFinDao.saveRiskProfile(RiskProfileEntity(riskScore = score, personalityType = personality))
    }

    // Roadmaps
    val roadmaps: Flow<List<InvestmentRoadmapEntity>> = nexFinDao.getAllRoadmaps()

    suspend fun generateRoadmap(portfolioSummary: String): String {
        val prompt = "Based on this portfolio: $portfolioSummary, generate a 1-year investment roadmap in Persian."
        val roadmapText = aiRepository.getChatResponse(prompt)
        nexFinDao.saveRoadmap(InvestmentRoadmapEntity(title = "نقشه راه ${System.currentTimeMillis()}", roadmapJson = roadmapText))
        return roadmapText
    }
}
