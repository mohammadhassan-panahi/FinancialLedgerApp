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
 * Data Transfer Objects for the BrsApi.ir "Gold_Currency" free market webservice.
 * Docs: https://brsapi.ir/free-api-gold-currency-webservice/
 *
 * Real sample response (captured from the live endpoint):
 * {
 *   "gold": [ { "date":"1405/05/15", "time":"16:59", "time_unix":1786022992,
 *               "symbol":"IR_GOLD_18K", "name_en":"18K Gold", "name":"طلای 18 عیار",
 *               "price":18578200, "change_value":16600, "change_percent":0.09, "unit":"تومان" }, ... ],
 *   "currency": [ ... same shape as gold ... ],
 *   "cryptocurrency": [ { ..., "price":"64440" (STRING, not a number!), "market_cap":..., "description":"..." }, ... ]
 * }
 *
 * Note the "gold" and "currency" arrays report `price` as a JSON number, while
 * "cryptocurrency" reports `price` as a JSON *string* — the DTOs below reflect that
 * (verified against a real captured response, not assumed).
 */
@JsonClass(generateAdapter = true)
data class BrsApiRateDto(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double,
    @Json(name = "change_value") val changeValue: Double? = null,
    @Json(name = "change_percent") val changePercent: Double = 0.0,
    @Json(name = "unit") val unit: String,
    @Json(name = "date") val date: String? = null,
    @Json(name = "time") val time: String? = null,
    @Json(name = "time_unix") val timeUnix: Long? = null
)

@JsonClass(generateAdapter = true)
data class BrsApiCryptoDto(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: String, // BrsApi returns this as a string, e.g. "64440"
    @Json(name = "change_percent") val changePercent: Double = 0.0,
    @Json(name = "market_cap") val marketCap: Double? = null,
    @Json(name = "unit") val unit: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "time") val time: String? = null,
    @Json(name = "time_unix") val timeUnix: Long? = null
)

@JsonClass(generateAdapter = true)
data class BrsApiGoldCurrencyResponse(
    @Json(name = "gold") val gold: List<BrsApiRateDto> = emptyList(),
    @Json(name = "currency") val currency: List<BrsApiRateDto> = emptyList(),
    @Json(name = "cryptocurrency") val cryptocurrency: List<BrsApiCryptoDto> = emptyList(),
    // Present only on error responses, e.g. {"code_http":401,"successful":false,"status":"unauthorized","message_error":"..."}
    @Json(name = "successful") val successful: Boolean? = null,
    @Json(name = "message_error") val messageError: String? = null
)

/**
 * Retrofit API Service Interface for the gold/currency/crypto rates.
 *
 * IMPORTANT — DIRECT CONNECTION, NOT PROXIED: this hits BrsApi.ir directly. A Cloudflare
 * Worker proxy was tried first (see git history / /brsapi-proxy) specifically to keep the
 * BrsApi key out of the APK, but BrsApi.ir returned 401 Unauthorized for every request that
 * came from the Worker's IPs, so the proxy was dropped in favor of calling BrsApi.ir
 * directly from the app. That means [BRSAPI_KEY][com.example.BuildConfig.BRSAPI_KEY] IS
 * compiled into this APK and can be extracted by decompiling it (apktool/jadx) — there is
 * currently no way around that with this architecture. See README.md for details/mitigations.
 *
 * Endpoint path verified against BrsApi.ir's official examples for the Pro tier
 * (https://Api.BrsApi.ir/Market/Gold_Currency_Pro.php); the Free path below follows the
 * same host/naming convention but has NOT been independently confirmed against BrsApi's
 * docs — test it against your own key before relying on it, and swap to the Pro path/params
 * if it 404s.
 */
interface MarketApiService {

    @GET("Market/Gold_Currency.php")
    suspend fun getGoldCurrency(@Query("key") apiKey: String): Response<BrsApiGoldCurrencyResponse>

    companion object {
        private const val BASE_URL = "https://api.brsapi.ir/"

        fun create(): MarketApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(MarketApiService::class.java)
        }
    }
}
