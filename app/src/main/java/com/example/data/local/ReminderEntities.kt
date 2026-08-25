package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReminderType { INSTALLMENT, BILL, CHEQUE, RENT, OTHER }

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountRial: Double,
    val type: ReminderType,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@androidx.room.Dao
interface ReminderDao {
    @androidx.room.Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<ReminderEntity>>

    @androidx.room.Insert
    suspend fun insert(entity: ReminderEntity)

    @androidx.room.Update
    suspend fun update(entity: ReminderEntity)

    @androidx.room.Delete
    suspend fun delete(entity: ReminderEntity)

    @androidx.room.Query("SELECT * FROM reminders WHERE isPaid = 0 AND dueDate <= :timestamp")
    suspend fun getPendingReminders(timestamp: Long): List<ReminderEntity>
}
