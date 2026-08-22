package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {

    @Query("SELECT * FROM calculation_history WHERE sectionKey = :sectionKey ORDER BY timestamp DESC")
    fun getHistoryForSection(sectionKey: String): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CalculationHistoryEntity): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM calculation_history WHERE sectionKey = :sectionKey")
    suspend fun clearHistoryForSection(sectionKey: String)
}
