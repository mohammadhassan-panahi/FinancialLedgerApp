package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class FngResponse(
    @Json(name = "data") val data: List<FngData>? = null
)

@JsonClass(generateAdapter = true)
data class FngData(
    @Json(name = "value") val value: String,
    @Json(name = "value_classification") val valueClassification: String,
    @Json(name = "timestamp") val timestamp: String
)

interface FearGreedApiService {
    @GET("fng/")
    suspend fun getFearAndGreed(): Response<FngResponse>

    companion object {
        fun create(): FearGreedApiService {
            return Retrofit.Builder()
                .baseUrl("https://api.alternative.me/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(FearGreedApiService::class.java)
        }
    }
}
