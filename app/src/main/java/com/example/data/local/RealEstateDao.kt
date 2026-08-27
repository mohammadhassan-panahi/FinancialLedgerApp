package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RealEstateDao {
    @Query("SELECT * FROM real_estate ORDER BY lastUpdate DESC")
    fun getAllProperties(): Flow<List<RealEstateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: RealEstateEntity)

    @Update
    suspend fun updateProperty(property: RealEstateEntity)

    @Delete
    suspend fun deleteProperty(property: RealEstateEntity)

    @Query("SELECT COUNT(*) FROM real_estate")
    suspend fun getPropertyCount(): Int
}
