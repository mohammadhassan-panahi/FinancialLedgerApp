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

    // Social Feed
    val socialFeed: Flow<List<SocialPostEntity>> = nexFinDao.getSocialFeed()

    suspend fun shareAnalysis(content: String, assetCode: String?, sentiment: String?) {
        nexFinDao.insertPost(SocialPostEntity(authorName = "کاربر نکس‌فین", content = content, assetCode = assetCode, sentiment = sentiment))
    }

    suspend fun seedSampleSocialPosts() {
        try {
            val currentFeed = nexFinDao.getSocialFeed().first()
            if (currentFeed.isEmpty()) {
                nexFinDao.insertPost(SocialPostEntity(authorName = "علی صراف", content = "به نظرم طلا ۱۸ عیار نقطه ورود خوبی داره.", assetCode = "GOLD_18K", sentiment = "Bullish"))
                nexFinDao.insertPost(SocialPostEntity(authorName = "سارا مهدوی", content = "شاخص کل بورس امروز مقاومت مهمی رو شکوند.", assetCode = "TSE_INDEX", sentiment = "Bullish"))
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
