package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtCreditDao {
    @Query("SELECT * FROM debt_credits ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DebtCreditEntity>>

    @Query("SELECT * FROM debt_credits WHERE type = 'DEBT' AND isSettled = 0")
    fun getActiveDebts(): Flow<List<DebtCreditEntity>>

    @Query("SELECT * FROM debt_credits WHERE type = 'CREDIT' AND isSettled = 0")
    fun getActiveCredits(): Flow<List<DebtCreditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebtCreditEntity)

    @Update
    suspend fun update(entity: DebtCreditEntity)

    @Delete
    suspend fun delete(entity: DebtCreditEntity)

    // Legacy support for aggregate flows - we will sum in Kotlin for precision
    @Query("SELECT amountRial FROM debt_credits WHERE type = 'DEBT' AND isSettled = 0")
    fun getTotalDebtFlow(): Flow<List<java.math.BigDecimal>>

    @Query("SELECT amountRial FROM debt_credits WHERE type = 'CREDIT' AND isSettled = 0")
    fun getTotalCreditFlow(): Flow<List<java.math.BigDecimal>>
}
