package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NexFinDao {
    
    // Risk Profile
    @Query("SELECT * FROM risk_profiles WHERE userId = :userId")
    fun getRiskProfile(userId: String = "default_user"): Flow<RiskProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRiskProfile(profile: RiskProfileEntity)

    // Investment Roadmap
    @Query("SELECT * FROM investment_roadmaps ORDER BY createdAt DESC")
    fun getAllRoadmaps(): Flow<List<InvestmentRoadmapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoadmap(roadmap: InvestmentRoadmapEntity)
}
