package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<WatchlistCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: WatchlistCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: WatchlistCategoryEntity)

    @Query("SELECT * FROM watchlist_assets WHERE categoryId = :categoryId")
    fun getAssetsForCategory(categoryId: Long): Flow<List<WatchlistAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAssetToWatchlist(asset: WatchlistAssetEntity)

    @Query("DELETE FROM watchlist_assets WHERE categoryId = :categoryId AND assetCode = :assetCode")
    suspend fun removeAssetFromWatchlist(categoryId: Long, assetCode: String)
}
