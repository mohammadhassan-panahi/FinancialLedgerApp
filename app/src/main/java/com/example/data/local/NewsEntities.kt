package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "news_items")
data class NewsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val source: String,
    val url: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val category: String, // "CRYPTO" or "ECONOMY"
    val importance: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val sentiment: String = "NEUTRAL", // "POSITIVE", "NEUTRAL", "NEGATIVE"
    val aiSummary: String? = null,
    val relatedAssets: String? = null // Comma-separated assets like "BTC,ETH"
)
