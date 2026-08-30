package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Stores the user's investment personality and risk score.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "risk_profiles")
data class RiskProfileEntity(
    @PrimaryKey
    val userId: String = "default_user",
    val riskScore: Int, // 0 to 100
    val personalityType: String, // e.g., "Conservative", "Aggressive"
    val lastAssessmentDate: Long = System.currentTimeMillis()
)

/**
 * Stores AI-generated investment roadmaps for the user.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "investment_roadmaps")
data class InvestmentRoadmapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val roadmapJson: String, // Full JSON description of the plan
    val createdAt: Long = System.currentTimeMillis()
)
