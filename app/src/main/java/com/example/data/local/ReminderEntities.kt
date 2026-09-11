package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * NOTE: previously RECURRING/ONE_TIME, but nothing in the app ever referenced those two values
 * while the Reminders UI referenced these five (unresolved-reference compile error) — renamed to
 * match actual usage. Room persists enums as their name (TEXT column), so this is not a schema
 * change and needs no migration.
 */
enum class ReminderType { INSTALLMENT, BILL, CHEQUE, RENT, OTHER }

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountRial: BigDecimal,
    val type: ReminderType,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
