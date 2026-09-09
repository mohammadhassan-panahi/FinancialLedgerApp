package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

enum class DebtCreditType { DEBT, CREDIT }

@Entity(tableName = "debt_credits")
data class DebtCreditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val amountRial: BigDecimal,
    val type: DebtCreditType,
    val dueDate: Long? = null,
    val description: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
