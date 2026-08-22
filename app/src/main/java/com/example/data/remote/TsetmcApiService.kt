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

/**
 * DTOs for the Tehran Stock Exchange (TSETMC) data, fetched DIRECTLY from BrsApi.ir.
 *
 * IMPORTANT — DIRECT CONNECTION, NOT PROXIED: a Cloudflare Worker proxy was tried first
 * specifically to keep the BrsApi key out of the APK, but BrsApi.ir returned 401
 * Unauthorized for requests coming from the Worker's IPs, so the proxy was dropped. The key
 * is now compiled into this APK's BuildConfig and is extractable via apktool/jadx — see
 * README.md.
 *
 * Endpoints verified against BrsApi.ir's own published examples:
 *   https://Api.BrsApi.ir/Tsetmc/AllSymbols.php?key=YourApiKey
 *   https://Api.BrsApi.ir/Tsetmc/Index.php?key=YourApiKey&type=Number
 *
 * Field names — VERIFIED 2026-08-09 against a real captured AllSymbols.php response:
 *   l18=symbol, l30=full name, pl=last price, pc=closing price, pcp=closing price change %,
 *   pd1=best bid (buy queue), po1=best ask (sell queue). Prices are already in RIAL (confirmed
 * via pl/eps ≈ pe on a real فولاد row) — do NOT multiply by RIAL_PER_TOMAN for this service.
 * The Index endpoint's exact field names are still unverified — same caution applies there.
 */
@JsonClass(generateAdapter = true)
data class TsetmcIndexDto(
    @Json(name = "index") val index: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: Double? = null,
    @Json(name = "change_percent") val changePercent: Double? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "time") val time: String? = null
)

@JsonClass(generateAdapter = true)
data class TsetmcSymbolDto(
    @Json(name = "l18") val symbol: String? = null,        // نماد کوتاه، مثلاً "فولاد"
    @Json(name = "l30") val fullName: String? = null,       // نام کامل، مثلاً "فولاد مبارکه اصفهان"
    @Json(name = "pl") val lastPrice: Double? = null,        // آخرین قیمت معامله (به ریال)
    @Json(name = "pc") val closingPrice: Double? = null,     // قیمت پایانی (به ریال)
    @Json(name = "pcp") val changePercent: Double? = null,   // درصد تغییر قیمت پایانی
    @Json(name = "pd1") val buyPrice: Double? = null,         // بهترین قیمت خرید صف اول (به ریال)
    @Json(name = "po1") val sellPrice: Double? = null,        // بهترین قیمت فروش صف اول (به ریال)
    @Json(name = "tno") val tradeCount: Long? = null,
    @Json(name = "successful") val successful: Boolean? = null,
    @Json(name = "message_error") val messageError: String? = null
) {
    /** True only if the API actually returned a usable price — NOT just "no error field present". */
    val hasValidPrice: Boolean get() = successful != false && closingPrice != null && closingPrice!! > 0.0
}

interface TsetmcApiService {
    @GET("Tsetmc/AllSymbols.php")
    suspend fun getAllSymbols(@Query("key") apiKey: String): Response<List<TsetmcSymbolDto>>

    @GET("Tsetmc/Index.php")
    suspend fun getIndices(@Query("key") apiKey: String): Response<List<TsetmcIndexDto>>

    companion object {
        private const val BASE_URL = "https://api.brsapi.ir/"

        fun create(): TsetmcApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(TsetmcApiService::class.java)
        }
    }
}

/**
 * Thin wrapper kept for call-site compatibility with the rest of the app (PortfolioRepository).
 * Holds the API key so callers don't need to thread it through every call site.
 */
class TsetmcApiClient(private val service: TsetmcApiService, private val apiKey: String) {
    suspend fun getIndices(): Response<List<TsetmcIndexDto>> = service.getIndices(apiKey)
    suspend fun getAllSymbols(): Response<List<TsetmcSymbolDto>> = service.getAllSymbols(apiKey)
}
