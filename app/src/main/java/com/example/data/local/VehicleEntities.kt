package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelName: String,
    val priceRial: BigDecimal,
    val changePercent: BigDecimal = BigDecimal.ZERO,
    val lastUpdate: Long = System.currentTimeMillis()
)
