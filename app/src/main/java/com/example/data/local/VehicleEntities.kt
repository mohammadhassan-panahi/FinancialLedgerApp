package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Vehicles for AI estimation and portfolio tracking.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelName: String,
    val priceRial: Double,
    val changePercent: Double,
    val lastUpdate: Long = System.currentTimeMillis()
)
