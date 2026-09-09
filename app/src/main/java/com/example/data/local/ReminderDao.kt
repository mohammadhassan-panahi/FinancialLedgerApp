package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderEntity)

    @Update
    suspend fun update(entity: ReminderEntity)

    @Delete
    suspend fun delete(entity: ReminderEntity)
}
