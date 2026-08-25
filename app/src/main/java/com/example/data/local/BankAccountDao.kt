package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Query("UPDATE bank_accounts SET currentBalance = currentBalance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: Double)

    @Query("SELECT SUM(currentBalance) FROM bank_accounts")
    fun getTotalLiquidity(): Flow<Double?>
}
