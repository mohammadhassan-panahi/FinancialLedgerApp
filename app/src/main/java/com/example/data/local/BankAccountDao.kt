package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): BankAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccountEntity): Long

    @Update
    suspend fun updateAccount(account: BankAccountEntity)

    @Delete
    suspend fun deleteAccount(account: BankAccountEntity)

    // Manual update should use BigDecimal
    @Query("UPDATE bank_accounts SET currentBalance = currentBalance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: BigDecimal)

    @Query("SELECT currentBalance FROM bank_accounts")
    fun getAllBalances(): Flow<List<BigDecimal>>
}
