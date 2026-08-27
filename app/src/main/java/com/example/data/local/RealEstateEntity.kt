package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Real Estate assets for valuation and portfolio tracking.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "real_estate")
data class RealEstateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyName: String,
    val valuationRial: Double,
    val changePercent: Double = 0.0,
    val lastUpdate: Long = System.currentTimeMillis()
)
