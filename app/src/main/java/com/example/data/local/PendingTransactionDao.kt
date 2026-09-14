package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    @Query("SELECT * FROM pending_transactions ORDER BY timestamp DESC")
    fun getAllPending(): Flow<List<PendingTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(transaction: PendingTransactionEntity)

    @Delete
    suspend fun deletePending(transaction: PendingTransactionEntity)

    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun deletePendingById(id: Long)

    @Query("DELETE FROM pending_transactions")
    suspend fun deleteAllPending()
}
