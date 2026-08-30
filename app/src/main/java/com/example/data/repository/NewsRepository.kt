package com.example.data.repository

import com.example.data.local.NewsDao
import com.example.data.local.NewsEntity
import com.example.data.remote.IranEconomyRssService
import com.example.data.remote.NewsApiService
import com.example.util.RssParser
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class NewsRepository(
    private val newsDao: NewsDao,
    private val newsApiService: NewsApiService,
    private val apiKey: String,
    private val aiRepository: AiRepository,
    private val iranEconomyRssService: IranEconomyRssService
) {
    fun getNews(category: String): Flow<List<NewsEntity>> = newsDao.getNewsByCategory(category)

    /** Crypto headlines from CryptoPanic, translated into Persian via Gemini before storage. */
    suspend fun refreshCryptoNews(): Result<Unit> {
        return try {
            val response = newsApiService.getCryptoNews(apiKey)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!.results
                val translatedTitles = aiRepository.translateHeadlinesToPersian(dtos.map { it.title })
                val news = dtos.mapIndexed { index, dto ->
                    NewsEntity(
                        id = dto.id.toString(),
                        title = translatedTitles.getOrElse(index) { dto.title },
                        // Keep the original English headline for reference / fallback display.
                        description = dto.title,
                        source = dto.source.title,
                        url = dto.url,
                        imageUrl = null,
                        publishedAt = parseDate(dto.publishedAt),
                        category = CATEGORY_CRYPTO,
                        importance = if ((dto.votes?.important ?: 0) > 5) "HIGH" else "MEDIUM",
                        sentiment = calculateSentiment(dto.votes?.positive ?: 0, dto.votes?.negative ?: 0),
                        relatedAssets = dto.currencies?.joinToString(",") { it.code }
                    )
                }
                if (news.isNotEmpty()) newsDao.insertNews(news)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch crypto news"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Iranian economic news from an RSS feed. NOTE: the confirmed-working feed URL
     * (donya-e-eqtesad.com/rss) is the newspaper's GENERAL aggregated feed — it mixes
     * politics, sports, health, etc. alongside economic news, and has no <category> tag to
     * filter on. So real items are kept only if they match an economic keyword — this is
     * filtering real data, not fabricating anything, but it's a heuristic, not an official
     * "economy only" feed from the source.
     */
    suspend fun refreshIranEconomyNews(feedUrl: String = IranEconomyRssService.DEFAULT_FEED_URL): Result<Unit> {
        return try {
            val xml = iranEconomyRssService.fetchRss(feedUrl)
            val items = RssParser.parse(xml).filter { isEconomicNews(it.title, it.description) }
            val news = items.map { item ->
                NewsEntity(
                    id = item.link.hashCode().toString(),
                    title = item.title,
                    description = item.description,
                    source = "دنیای اقتصاد",
                    url = item.link,
                    imageUrl = null,
                    publishedAt = parseRssDate(item.pubDate),
                    category = CATEGORY_IRAN_ECONOMY,
                    importance = "MEDIUM",
                    sentiment = "NEUTRAL",
                    relatedAssets = null
                )
            }
            if (news.isNotEmpty()) newsDao.insertNews(news)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val economicKeywords = listOf(
        "اقتصاد", "بورس", "سهام", "دلار", "تومان", "ریال", "تورم", "بانک", "نرخ ارز",
        "صادرات", "واردات", "یارانه", "بودجه", "مالیات", "بازار", "تولید", "صنعت",
        "نفت", "طلا", "سکه", "رکود", "تحریم", "وام", "تسهیلات", "بیمه", "بازنشست",
        "گرانی", "قیمت", "دستمزد", "افزایش حقوق", "حقوق کارگران", "سرمایه‌گذاری", "سرمایه گذاری",
        "بانک مرکزی", "خودرو", "مسکن", "اجاره", "کارخانه", "شرکت", "سود بانکی"
    )

    private fun isEconomicNews(title: String, description: String?): Boolean {
        val text = "$title ${description.orEmpty()}"
        return economicKeywords.any { text.contains(it) }
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

    private fun parseRssDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val patterns = listOf("EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss zzz")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.parse(dateStr)?.let { return it.time }
            } catch (e: Exception) { /* try next pattern */ }
        }
        return System.currentTimeMillis()
    }

    private fun calculateSentiment(pos: Int, neg: Int): String {
        return when {
            pos > neg * 2 && pos > 5 -> "POSITIVE"
            neg > pos * 2 && neg > 5 -> "NEGATIVE"
            else -> "NEUTRAL"
        }
    }

    companion object {
        const val CATEGORY_CRYPTO = "CRYPTO"
        const val CATEGORY_IRAN_ECONOMY = "IRAN_ECONOMY"
    }
}
