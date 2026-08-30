package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Fetches raw RSS/XML from an Iranian news feed. Unlike BrsApi/CryptoPanic, standard
 * news RSS feeds need no API key.
 *
 * [DEFAULT_FEED_URL] was confirmed live (returns valid RSS 2.0 XML) — but it's the
 * newspaper's GENERAL aggregated feed, not an economy-only one; [com.example.data.repository.NewsRepository]
 * applies a keyword filter on top of it to keep only economically-relevant items. If you find
 * or are given a genuine economy-category feed URL for this or another source, swap it in here
 * and the keyword filter becomes a harmless extra safety net rather than the main filter.
 */
class IranEconomyRssService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchRss(url: String = DEFAULT_FEED_URL): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("RSS fetch failed: HTTP ${response.code}")
            return response.body?.string() ?: throw IOException("Empty RSS response body")
        }
    }

    companion object {
        /** Donya-e-Eqtesad's general RSS feed — confirmed working, but not economy-filtered. */
        const val DEFAULT_FEED_URL = "https://donya-e-eqtesad.com/rss"
    }
}
