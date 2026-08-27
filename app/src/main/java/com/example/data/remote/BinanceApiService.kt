package com.example.data.remote

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit service for fetching historical price data (Klines) from Binance Public API.
 * Docs: https://binance-docs.github.io/apidocs/spot/en/#kline-candlestick-data
 */
interface BinanceApiService {

    /**
     * Fetches Klines (OHLC) for a specific symbol and interval.
     * Response is a list of lists: [ [OpenTime, Open, High, Low, Close, Volume, CloseTime, ...], ... ]
     */
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 500
    ): Response<List<List<Any>>>

    companion object {
        private const val BASE_URL = "https://api.binance.com/"

        fun create(): BinanceApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(BinanceApiService::class.java)
        }
    }
}
