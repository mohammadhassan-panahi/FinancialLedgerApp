package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "initial_public_offerings")
data class IpoEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val ipoDate: String, // شمسی
    val maxShares: Int,
    val maxPriceRial: Double,
    val minPriceRial: Double,
    val requiredLiquidityRial: Double,
    val status: String, // در حال عرضه، منقضی شده، به زودی
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "codal_notices")
data class CodalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val title: String,
    val publishDate: String,
    val link: String,
    val category: String, // صورت مالی، مجمع، افزایش سرمایه و...
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@androidx.room.Dao
interface BourseDao {
    @androidx.room.Query("SELECT * FROM initial_public_offerings ORDER BY createdAt DESC")
    fun getAllIpos(): kotlinx.coroutines.flow.Flow<List<IpoEntity>>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertIpos(ipos: List<IpoEntity>)

    @androidx.room.Query("SELECT * FROM codal_notices ORDER BY createdAt DESC LIMIT 50")
    fun getAllCodalNotices(): kotlinx.coroutines.flow.Flow<List<CodalEntity>>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertCodalNotices(notices: List<CodalEntity>)

    @androidx.room.Query("UPDATE codal_notices SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @androidx.room.Query("SELECT * FROM initial_public_offerings WHERE status = 'به زودی' OR status = 'در حال عرضه'")
    suspend fun getActiveIpos(): List<IpoEntity>
}
