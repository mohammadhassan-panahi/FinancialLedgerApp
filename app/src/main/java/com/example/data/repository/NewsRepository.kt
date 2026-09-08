package com.example.data.repository

import com.example.data.local.NewsDao
import com.example.data.local.NewsEntity
import com.example.data.remote.RssService
import com.example.util.RssParser
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class NewsRepository(
    private val newsDao: NewsDao,
    private val rssService: RssService
) {
    fun getNews(category: String): Flow<List<NewsEntity>> = newsDao.getNewsByCategory(category)

    /**
     * Aggregates crypto news from multiple Iranian sources (ArzDigital, Zoomit, Ramzarz News, iSignal)
     * and deduplicates them based on normalized titles.
     */
    suspend fun refreshAggregatedCryptoNews(): Result<Unit> {
        val feeds = listOf(
            RssService.ARZ_DIGITAL_RSS to "ارزدیجیتال",
            RssService.ZOOMIT_CRYPTO_RSS to "زومیت",
            RssService.RAMZARZ_NEWS_RSS to "رمز ارز نیوز",
            RssService.ISIGNAL_CRYPTO_RSS to "آی‌سیگنال"
        )
        return aggregateAndSave(feeds, CATEGORY_CRYPTO)
    }

    /**
     * Aggregates general technology news from Zoomit.
     */
    suspend fun refreshTechNews(): Result<Unit> {
        val feeds = listOf(RssService.ZOOMIT_TECH_RSS to "زومیت (تکنولوژی)")
        return aggregateAndSave(feeds, CATEGORY_TECH)
    }

    /**
     * Aggregates Iranian economic and general news from multiple sources (Donya-e-Eqtesad, iSignal).
     */
    suspend fun refreshIranEconomyNews(): Result<Unit> {
        val feeds = listOf(
            RssService.DONYA_E_EQTESAD_RSS to "دنیای اقتصاد",
            RssService.ISIGNAL_BOURSE_RSS to "آی‌سیگنال (بورس)",
            RssService.ISIGNAL_GOLD_RSS to "آی‌سیگنال (طلا)",
            RssService.ISIGNAL_ECONOMY_RSS to "آی‌سیگنال (اقتصاد)",
            RssService.ISIGNAL_POLITICAL_RSS to "آی‌سیگنال (سیاسی)",
            RssService.ISIGNAL_INTL_RSS to "آی‌سیگنال (بین‌الملل)",
            RssService.ISIGNAL_CAR_RSS to "آی‌سیگنال (خودرو)",
            RssService.ISIGNAL_HOUSING_RSS to "آی‌سیگنال (مسکن)"
        )
        // For general economy feeds, we still apply keywords to filter out non-economic noise
        return aggregateAndSave(feeds, CATEGORY_IRAN_ECONOMY, applyEconomicFilter = true)
    }

    private suspend fun aggregateAndSave(
        feeds: List<Pair<String, String>>, 
        category: String,
        applyEconomicFilter: Boolean = false
    ): Result<Unit> {
        val aggregatedNews = mutableMapOf<String, NewsEntity>()

        return try {
            feeds.forEach { (url, sourceName) ->
                try {
                    val xml = rssService.fetchRss(url)
                    val items = RssParser.parse(xml)
                    
                    items.forEach { item ->
                        if (applyEconomicFilter && !isEconomicNews(item.title, item.description)) {
                            return@forEach
                        }

                        val normalizedTitle = normalizeText(item.title)
                        if (!aggregatedNews.containsKey(normalizedTitle)) {
                            val entity = NewsEntity(
                                id = item.link.hashCode().toString(),
                                title = item.title,
                                description = item.description,
                                source = sourceName,
                                url = item.link,
                                imageUrl = null,
                                publishedAt = parseRssDate(item.pubDate),
                                category = category,
                                importance = "MEDIUM",
                                sentiment = "NEUTRAL",
                                relatedAssets = null,
                                aiSummary = null
                            )
                            aggregatedNews[normalizedTitle] = entity
                        }
                    }
                } catch (e: Exception) {
                    // Log or handle individual feed error if needed
                }
            }

            if (aggregatedNews.isNotEmpty()) {
                newsDao.insertNews(aggregatedNews.values.toList())
                Result.success(Unit)
            } else {
                Result.failure(Exception("هیچ خبری یافت نشد"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeText(text: String): String {
        return text.replace(Regex("[^\\p{L}\\p{N}]"), "")
            .replace("ی", "ی") 
            .replace("ک", "ک") 
            .replace(" ", "")
            .trim()
    }

    private val economicKeywords = listOf(
        "اقتصاد", "بورس", "سهام", "دلار", "تومان", "ریال", "تورم", "بانک", "نرخ ارز",
        "صادرات", "واردات", "یارانه", "بودجه", "مالیات", "بازار", "تولید", "صنعت",
        "نفت", "طلا", "سکه", "رکود", "تحریم", "وام", "تسهیلات", "بیمه", "بازنشست",
        "گرانی", "قیمت", "دستمزد", "افزایش حقوق", "حقوق کارگران", "سرمایه‌گذاری",
        "بانک مرکزی", "خودرو", "مسکن", "اجاره", "کارخانه", "شرکت", "سود بانکی", "سیاسی", "بین‌الملل", "کالا", "انرژی"
    )

    private fun isEconomicNews(title: String, description: String?): Boolean {
        val text = "$title ${description.orEmpty()}"
        return economicKeywords.any { text.contains(it) }
    }

    private fun parseRssDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val patterns = listOf("EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyy-MM-dd HH:mm:ss")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.parse(dateStr)?.let { return it.time }
            } catch (e: Exception) { }
        }
        return System.currentTimeMillis()
    }

    companion object {
        const val CATEGORY_CRYPTO = "CRYPTO"
        const val CATEGORY_IRAN_ECONOMY = "IRAN_ECONOMY"
        const val CATEGORY_TECH = "TECH"
    }
}
