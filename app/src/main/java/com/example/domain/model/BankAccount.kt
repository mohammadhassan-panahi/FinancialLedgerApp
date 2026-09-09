package com.example.domain.model

import java.math.BigDecimal

data class BankAccount(
    val id: Long,
    val name: String,
    val bankName: String,
    val currentBalance: BigDecimal,
    val colorHex: String
)
