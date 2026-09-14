package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawSmsContent: String? = null,
    val bankName: String? = null
)
