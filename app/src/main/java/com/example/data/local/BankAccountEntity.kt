package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/** Balances are stored in TOMAN — display in Rial only through RialUtils.currentBalanceRial. */
@JsonClass(generateAdapter = true)
@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bankName: String,
    val initialBalance: Double,
    val currentBalance: Double,
    val colorHex: String = "#5B85AA" // Default color
)
