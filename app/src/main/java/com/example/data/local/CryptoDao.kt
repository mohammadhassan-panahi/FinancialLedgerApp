package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {
    @Query("SELECT * FROM crypto_assets ORDER BY cmcRank ASC")
    fun getAllAssets(): Flow<List<CryptoAssetEntity>>

    @Query("SELECT * FROM crypto_assets ORDER BY cmcRank ASC")
    suspend fun getAllAssetsOnce(): List<CryptoAssetEntity>

    @Query("SELECT * FROM crypto_assets WHERE isInWatchlist = 1 ORDER BY cmcRank ASC")
    fun getWatchlist(): Flow<List<CryptoAssetEntity>>

    @Query("SELECT * FROM crypto_assets WHERE symbol = :symbol LIMIT 1")
    suspend fun getBySymbol(symbol: String): CryptoAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<CryptoAssetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: CryptoAssetEntity)

    @Query("UPDATE crypto_assets SET isInWatchlist = :inWatchlist WHERE symbol = :symbol")
    suspend fun setWatchlist(symbol: String, inWatchlist: Boolean)

    @Query("SELECT COUNT(*) FROM crypto_assets")
    suspend fun getAssetCount(): Int

    @Query("SELECT * FROM crypto_info WHERE cmcId = :cmcId LIMIT 1")
    suspend fun getInfo(cmcId: Int): CryptoInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(info: CryptoInfoEntity)
}
