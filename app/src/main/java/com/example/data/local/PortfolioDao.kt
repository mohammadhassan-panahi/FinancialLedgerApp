package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetPurchaseDao {
    @Query("SELECT * FROM asset_purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<AssetPurchaseEntity>>

    @Query("SELECT * FROM asset_purchases WHERE assetType = :type ORDER BY purchaseDate DESC")
    fun getPurchasesByType(type: PortfolioAssetType): Flow<List<AssetPurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: AssetPurchaseEntity): Long

    @Query("DELETE FROM asset_purchases WHERE id = :id")
    suspend fun deletePurchase(id: Long)

    @Query("SELECT COUNT(*) FROM asset_purchases")
    suspend fun getPurchaseCount(): Int
}

@Dao
interface AssetSaleDao {
    @Query("SELECT * FROM asset_sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<AssetSaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: AssetSaleEntity): Long

    @Query("DELETE FROM asset_sales WHERE id = :id")
    suspend fun deleteSale(id: Long)
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_symbols WHERE isInWatchlist = 1 ORDER BY symbol")
    fun getWatchlist(): Flow<List<StockSymbolEntity>>

    @Query("SELECT * FROM stock_symbols WHERE isInWatchlist = 1 ORDER BY symbol")
    suspend fun getWatchlistOnce(): List<StockSymbolEntity>

    @Query("SELECT * FROM stock_symbols")
    fun getAllSymbols(): Flow<List<StockSymbolEntity>>

    @Query("SELECT * FROM stock_symbols")
    suspend fun getAllSymbolsOnce(): List<StockSymbolEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymbols(symbols: List<StockSymbolEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymbol(symbol: StockSymbolEntity)

    @Query("UPDATE stock_symbols SET isInWatchlist = :inWatchlist WHERE symbol = :symbol")
    suspend fun setWatchlist(symbol: String, inWatchlist: Boolean)

    @Query("SELECT * FROM market_index")
    fun getIndices(): Flow<List<MarketIndexEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndices(indices: List<MarketIndexEntity>)

    @Query("SELECT COUNT(*) FROM stock_symbols")
    suspend fun getSymbolCount(): Int
}

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE isActive = 1")
    suspend fun getActiveAlerts(): List<PriceAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity): Long

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)

    @Query("UPDATE price_alerts SET isActive = 0, lastTriggeredAt = :triggeredAt WHERE id = :id")
    suspend fun markTriggered(id: Long, triggeredAt: Long)
}
