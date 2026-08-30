package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_items WHERE category = :category ORDER BY publishedAt DESC")
    fun getNewsByCategory(category: String): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(items: List<NewsEntity>)

    @Query("DELETE FROM news_items WHERE category = :category")
    suspend fun clearNewsByCategory(category: String)

    @Query("SELECT * FROM news_items WHERE id = :id")
    suspend fun getNewsById(id: String): NewsEntity?
}
