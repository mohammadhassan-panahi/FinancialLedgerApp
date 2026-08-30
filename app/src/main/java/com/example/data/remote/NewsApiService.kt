package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CryptoPanicNewsDto(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "published_at") val publishedAt: String,
    @Json(name = "source") val source: CryptoPanicSourceDto,
    @Json(name = "currencies") val currencies: List<CryptoPanicCurrencyDto>? = null,
    @Json(name = "votes") val votes: CryptoPanicVotesDto? = null
)

@JsonClass(generateAdapter = true)
data class CryptoPanicSourceDto(
    @Json(name = "title") val title: String,
    @Json(name = "domain") val domain: String
)

@JsonClass(generateAdapter = true)
data class CryptoPanicCurrencyDto(
    @Json(name = "code") val code: String,
    @Json(name = "title") val title: String
)

@JsonClass(generateAdapter = true)
data class CryptoPanicVotesDto(
    @Json(name = "positive") val positive: Int,
    @Json(name = "negative") val negative: Int,
    @Json(name = "important") val important: Int
)

@JsonClass(generateAdapter = true)
data class CryptoPanicResponse(
    @Json(name = "results") val results: List<CryptoPanicNewsDto>
)

interface NewsApiService {
    @GET("api/v1/posts/")
    suspend fun getCryptoNews(
        @Query("auth_token") apiKey: String,
        @Query("public") public: Boolean = true,
        @Query("filter") filter: String? = null
    ): Response<CryptoPanicResponse>

    companion object {
        private const val BASE_URL = "https://cryptopanic.com/"

        fun create(): NewsApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}
