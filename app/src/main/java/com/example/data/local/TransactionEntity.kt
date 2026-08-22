package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    DEPOSIT, TRANSFER, SWAP, EXPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double, // Amount in Toman
    val type: TransactionType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
