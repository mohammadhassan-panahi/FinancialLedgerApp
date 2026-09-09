package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmountRial: BigDecimal,
    val currentSavedRial: BigDecimal = BigDecimal.ZERO,
    val deadline: Long? = null,
    val category: String = "سایر",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
