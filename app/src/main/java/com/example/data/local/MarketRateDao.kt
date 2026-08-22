package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Market Rates and Mutual Fund NAV values.
 */
@Dao
interface MarketRateDao {

    @Query("SELECT * FROM market_rates ORDER BY updatedAt DESC")
    fun getAllMarketRates(): Flow<List<MarketRateEntity>>

    @Query("SELECT * FROM market_rates ORDER BY updatedAt DESC")
    suspend fun getAllMarketRatesOnce(): List<MarketRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketRates(rates: List<MarketRateEntity>)

    @Query("SELECT COUNT(*) FROM market_rates")
    suspend fun getMarketRateCount(): Int

    @Query("SELECT * FROM mutual_funds")
    fun getAllMutualFunds(): Flow<List<MutualFundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutualFunds(funds: List<MutualFundEntity>)

    @Query("SELECT COUNT(*) FROM mutual_funds")
    suspend fun getMutualFundCount(): Int

    @Query("DELETE FROM market_rates")
    suspend fun clearMarketRates()

    @Query("DELETE FROM mutual_funds")
    suspend fun clearMutualFunds()
}
