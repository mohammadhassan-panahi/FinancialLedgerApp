package com.example.data.repository

import com.example.data.local.NewsDao
import com.example.data.local.NewsEntity
import com.example.data.remote.NewsApiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class NewsRepository(
    private val newsDao: NewsDao,
    private val newsApiService: NewsApiService,
    private val apiKey: String
) {
    fun getNews(category: String): Flow<List<NewsEntity>> = newsDao.getNewsByCategory(category)

    suspend fun refreshCryptoNews(): Result<Unit> {
        return try {
            val response = newsApiService.getCryptoNews(apiKey)
            if (response.isSuccessful && response.body() != null) {
                val news = response.body()!!.results.map { dto ->
                    NewsEntity(
                        id = dto.id.toString(),
                        title = dto.title,
                        description = null,
                        source = dto.source.title,
                        url = dto.url,
                        imageUrl = null,
                        publishedAt = parseDate(dto.publishedAt),
                        category = "CRYPTO",
                        importance = if ((dto.votes?.important ?: 0) > 5) "HIGH" else "MEDIUM",
                        sentiment = calculateSentiment(dto.votes?.positive ?: 0, dto.votes?.negative ?: 0),
                        relatedAssets = dto.currencies?.joinToString(",") { it.code }
                    )
                }
                newsDao.insertNews(news)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch news"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun calculateSentiment(pos: Int, neg: Int): String {
        return when {
            pos > neg * 2 && pos > 5 -> "POSITIVE"
            neg > pos * 2 && neg > 5 -> "NEGATIVE"
            else -> "NEUTRAL"
        }
    }
}
