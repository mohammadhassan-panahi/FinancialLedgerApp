package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * A user-defined category for organizing assets in the watchlist.
 * Examples: "Favorites", "DeFi", "Long-term".
 */
@Entity(tableName = "watchlist_categories")
data class WatchlistCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Cross-reference table for many-to-many relationship between assets and watchlist categories.
 */
@Entity(tableName = "watchlist_assets", primaryKeys = ["categoryId", "assetCode"])
data class WatchlistAssetEntity(
    val categoryId: Long,
    val assetCode: String, // Symbol for stock/crypto or assetCode for others
    val assetType: PortfolioAssetType,
    val addedAt: Long = System.currentTimeMillis()
)
