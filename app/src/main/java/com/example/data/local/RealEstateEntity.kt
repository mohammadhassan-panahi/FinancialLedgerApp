package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "real_estate")
data class RealEstateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyName: String,
    val valuationRial: BigDecimal,
    val changePercent: BigDecimal = BigDecimal.ZERO,
    val lastUpdate: Long = System.currentTimeMillis()
)
