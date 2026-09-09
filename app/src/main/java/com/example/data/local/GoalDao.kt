package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM financial_goals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GoalEntity)

    @Update
    suspend fun update(entity: GoalEntity)

    @Delete
    suspend fun delete(entity: GoalEntity)
}
