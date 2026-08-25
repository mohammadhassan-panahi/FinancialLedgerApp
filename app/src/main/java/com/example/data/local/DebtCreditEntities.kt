package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtCreditType { DEBT, CREDIT }

@Entity(tableName = "debt_credits")
data class DebtCreditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amountRial: Double,
    val type: DebtCreditType,
    val dueDate: Long? = null,
    val description: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@androidx.room.Dao
interface DebtCreditDao {
    @androidx.room.Query("SELECT * FROM debt_credits ORDER BY createdAt DESC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<DebtCreditEntity>>

    @androidx.room.Insert
    suspend fun insert(entity: DebtCreditEntity)

    @androidx.room.Update
    suspend fun update(entity: DebtCreditEntity)

    @androidx.room.Delete
    suspend fun delete(entity: DebtCreditEntity)

    @androidx.room.Query("SELECT SUM(amountRial) FROM debt_credits WHERE type = 'DEBT' AND isSettled = 0")
    fun getTotalDebt(): kotlinx.coroutines.flow.Flow<Double?>

    @androidx.room.Query("SELECT SUM(amountRial) FROM debt_credits WHERE type = 'CREDIT' AND isSettled = 0")
    fun getTotalCredit(): kotlinx.coroutines.flow.Flow<Double?>
}
