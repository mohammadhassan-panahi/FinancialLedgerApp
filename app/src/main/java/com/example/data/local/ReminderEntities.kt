package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

enum class ReminderType { RECURRING, ONE_TIME }

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
