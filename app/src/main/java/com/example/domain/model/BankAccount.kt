package com.example.domain.model

data class BankAccount(
    val id: Long,
    val name: String,
    val bankName: String,
    val currentBalance: Double,
    val colorHex: String
)
