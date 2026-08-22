package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit service + DTOs for the CoinMarketCap Pro API — Basic (free) tier only.
 * Docs: https://coinmarketcap.com/api/documentation/v1/
 *
 * IMPORTANT — scope of the free tier: this covers ONLY "اطلاعات پایه" (name/symbol/rank/
 * website/etc, via /info) and "Market Data" (price/change/market cap/volume/supply, via
 * /listings/latest and /quotes/latest) from the full feature list that was requested.
 * Everything else in that list — Holder Concentration, Decentralization, Contract Control,
 * Security/Audit, Developer Activity, Ecosystem/TVL, on-chain Network stats — is NOT
 * available from CoinMarketCap at any tier and needs separate data sources (e.g. Etherscan-
 * family explorers for on-chain data, GoPlus/CertiK for contract security, GitHub API for
 * developer activity, DefiLlama for TVL/ecosystem). Historical price data (needed for
 * Technical Analysis indicators like RSI/MACD/EMA) also requires a paid Hobbyist+ plan on
 * CMC, so that will need to be computed from OUR OWN cached quote snapshots over time
 * instead, or sourced elsewhere.
 *
 * Auth: API key goes in the `X-CMC_PRO_API_KEY` HEADER (not a query param), against
 * https://pro-api.coinmarketcap.com. This is a DIRECT connection from the app (same
 * architecture/tradeoff as BrsApi — see README's security warning): the key is compiled
 * into the APK and is extractable via decompilation.
 */

@JsonClass(generateAdapter = true)
data class CmcStatusDto(
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "error_code") val errorCode: Int = 0,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "credit_count") val creditCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class CmcQuoteDto(
    @Json(name = "price") val price: Double? = null,
    @Json(name = "volume_24h") val volume24h: Double? = null,
    @Json(name = "volume_change_24h") val volumeChange24h: Double? = null,
    @Json(name = "percent_change_1h") val percentChange1h: Double? = null,
    @Json(name = "percent_change_24h") val percentChange24h: Double? = null,
    @Json(name = "percent_change_7d") val percentChange7d: Double? = null,
    @Json(name = "percent_change_30d") val percentChange30d: Double? = null,
    @Json(name = "percent_change_60d") val percentChange60d: Double? = null,
    @Json(name = "percent_change_90d") val percentChange90d: Double? = null,
    @Json(name = "market_cap") val marketCap: Double? = null,
    @Json(name = "market_cap_dominance") val marketCapDominance: Double? = null,
    @Json(name = "fully_diluted_market_cap") val fullyDilutedMarketCap: Double? = null,
    @Json(name = "last_updated") val lastUpdated: String? = null
)

/** One coin's data as returned by /listings/latest and /quotes/latest ("USD" convert). */
@JsonClass(generateAdapter = true)
data class CmcCoinDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "cmc_rank") val cmcRank: Int? = null,
    @Json(name = "num_market_pairs") val numMarketPairs: Int? = null,
    @Json(name = "circulating_supply") val circulatingSupply: Double? = null,
    @Json(name = "total_supply") val totalSupply: Double? = null,
    @Json(name = "max_supply") val maxSupply: Double? = null,
    @Json(name = "infinite_supply") val infiniteSupply: Boolean? = null,
    @Json(name = "last_updated") val lastUpdated: String? = null,
    @Json(name = "date_added") val dateAdded: String? = null,
    @Json(name = "tags") val tags: List<String>? = null,
    @Json(name = "platform") val platform: CmcPlatformDto? = null,
    // Keyed by convert currency, e.g. {"USD": {...}}
    @Json(name = "quote") val quote: Map<String, CmcQuoteDto>? = null
)

/** Present only when a token lives on another chain (e.g. an ERC-20) — null for native coins. */
@JsonClass(generateAdapter = true)
data class CmcPlatformDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "token_address") val tokenAddress: String? = null
)

@JsonClass(generateAdapter = true)
data class CmcListingsResponse(
    @Json(name = "status") val status: CmcStatusDto,
    @Json(name = "data") val data: List<CmcCoinDto> = emptyList()
)

/** /quotes/latest keys its "data" object by CMC numeric id (as a string), not a list. */
@JsonClass(generateAdapter = true)
data class CmcQuotesResponse(
    @Json(name = "status") val status: CmcStatusDto,
    @Json(name = "data") val data: Map<String, CmcCoinDto> = emptyMap()
)

/** One coin's static metadata as returned by /cryptocurrency/info. */
@JsonClass(generateAdapter = true)
data class CmcInfoDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "category") val category: String? = null, // e.g. "coin" or "token"
    @Json(name = "description") val description: String? = null,
    @Json(name = "logo") val logo: String? = null,
    @Json(name = "date_added") val dateAdded: String? = null,
    @Json(name = "platform") val platform: CmcPlatformDto? = null,
    @Json(name = "urls") val urls: CmcUrlsDto? = null
)

@JsonClass(generateAdapter = true)
data class CmcUrlsDto(
    @Json(name = "website") val website: List<String>? = null,
    @Json(name = "technical_doc") val technicalDoc: List<String>? = null, // whitepaper
    @Json(name = "explorer") val explorer: List<String>? = null,
    @Json(name = "source_code") val sourceCode: List<String>? = null, // GitHub etc.
    @Json(name = "chat") val chat: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CmcInfoResponse(
    @Json(name = "status") val status: CmcStatusDto,
    @Json(name = "data") val data: Map<String, CmcInfoDto> = emptyMap()
)

/** /global-metrics/quotes/latest — total market cap, BTC/ETH dominance, etc. */
@JsonClass(generateAdapter = true)
data class CmcGlobalQuoteDto(
    @Json(name = "total_market_cap") val totalMarketCap: Double? = null,
    @Json(name = "total_volume_24h") val totalVolume24h: Double? = null,
    @Json(name = "last_updated") val lastUpdated: String? = null
)

@JsonClass(generateAdapter = true)
data class CmcGlobalMetricsDto(
    @Json(name = "btc_dominance") val btcDominance: Double? = null,
    @Json(name = "eth_dominance") val ethDominance: Double? = null,
    @Json(name = "active_cryptocurrencies") val activeCryptocurrencies: Int? = null,
    @Json(name = "active_exchanges") val activeExchanges: Int? = null,
    @Json(name = "last_updated") val lastUpdated: String? = null,
    @Json(name = "quote") val quote: Map<String, CmcGlobalQuoteDto>? = null
)

@JsonClass(generateAdapter = true)
data class CmcGlobalMetricsResponse(
    @Json(name = "status") val status: CmcStatusDto,
    @Json(name = "data") val data: CmcGlobalMetricsDto? = null
)

interface CoinMarketCapApiService {

    /** Ranked/paginated list of coins — good for a "browse market" screen. 1 credit per 200 coins. */
    @GET("v1/cryptocurrency/listings/latest")
    suspend fun getListings(
        @Header("X-CMC_PRO_API_KEY") apiKey: String,
        @Query("start") start: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("convert") convert: String = "USD"
    ): Response<CmcListingsResponse>

    /** Specific coins by symbol (comma-separated), e.g. "BTC,ETH,USDT" — for watchlist/detail refresh. */
    @GET("v1/cryptocurrency/quotes/latest")
    suspend fun getQuotesBySymbol(
        @Header("X-CMC_PRO_API_KEY") apiKey: String,
        @Query("symbol") symbols: String,
        @Query("convert") convert: String = "USD"
    ): Response<CmcQuotesResponse>

    /** Static metadata (website, whitepaper, logo, description) — call sparingly, changes rarely. */
    @GET("v1/cryptocurrency/info")
    suspend fun getInfoBySymbol(
        @Header("X-CMC_PRO_API_KEY") apiKey: String,
        @Query("symbol") symbols: String
    ): Response<CmcInfoResponse>

    /** Total market cap / BTC & ETH dominance — for the "Global Market" section. */
    @GET("v1/global-metrics/quotes/latest")
    suspend fun getGlobalMetrics(
        @Header("X-CMC_PRO_API_KEY") apiKey: String,
        @Query("convert") convert: String = "USD"
    ): Response<CmcGlobalMetricsResponse>

    companion object {
        private const val BASE_URL = "https://pro-api.coinmarketcap.com/"

        fun create(): CoinMarketCapApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(CoinMarketCapApiService::class.java)
        }
    }
}
