package com.example.data

import com.example.data.local.BankAccountEntity
import com.example.domain.model.BankAccount

fun BankAccountEntity.toDomain(): BankAccount {
    return BankAccount(
        id = id,
        name = name,
        bankName = bankName,
        currentBalance = currentBalance,
        colorHex = colorHex
    )
}
