package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmountRial: Double,
    val currentSavedRial: Double = 0.0,
    val deadline: Long? = null,
    val category: String = "سایر", // گوشی، خودرو، سکه و...
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@androidx.room.Dao
interface GoalDao {
    @androidx.room.Query("SELECT * FROM financial_goals ORDER BY createdAt DESC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<GoalEntity>>

    @androidx.room.Insert
    suspend fun insert(entity: GoalEntity)

    @androidx.room.Update
    suspend fun update(entity: GoalEntity)

    @androidx.room.Delete
    suspend fun delete(entity: GoalEntity)
}
