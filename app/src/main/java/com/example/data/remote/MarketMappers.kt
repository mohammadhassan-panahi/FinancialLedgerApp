package com.example.data.remote

import com.example.domain.model.MarketAsset
import com.example.domain.model.MarketAssetType

/**
 * Maps BrsApi Rate DTO to domain MarketAsset.
 */
fun BrsApiRateDto.toDomain(type: MarketAssetType): MarketAsset {
    return MarketAsset(
        id = symbol,
        symbol = symbol,
        name = name,
        type = type,
        price = price,
        previousPrice = price / (1 + (changePercent / 100)),
        change = price - (price / (1 + (changePercent / 100))),
        changePercent = changePercent,
        timestamp = timeUnix?.times(1000) ?: System.currentTimeMillis(),
        source = "BrsApi"
    )
}

/**
 * Maps BrsApi Crypto DTO to domain MarketAsset.
 */
fun BrsApiCryptoDto.toDomain(): MarketAsset {
    val currentPrice = price.toDoubleOrNull() ?: 0.0
    return MarketAsset(
        id = symbol,
        symbol = symbol,
        name = name,
        type = MarketAssetType.CRYPTO,
        price = currentPrice,
        previousPrice = currentPrice / (1 + (changePercent / 100)),
        change = currentPrice - (currentPrice / (1 + (changePercent / 100))),
        changePercent = changePercent,
        timestamp = timeUnix?.times(1000) ?: System.currentTimeMillis(),
        source = "BrsApi"
    )
}

/**
 * Maps Tsetmc Symbol DTO to domain MarketAsset.
 */
fun TsetmcSymbolDto.toDomain(): MarketAsset {
    val currentPrice = closingPrice ?: 0.0
    val pcp = changePercent ?: 0.0
    return MarketAsset(
        id = symbol ?: "",
        symbol = symbol ?: "",
        name = fullName ?: "",
        type = MarketAssetType.STOCK,
        price = currentPrice,
        previousPrice = currentPrice / (1 + (pcp / 100)),
        change = currentPrice - (currentPrice / (1 + (pcp / 100))),
        changePercent = pcp,
        timestamp = System.currentTimeMillis(),
        source = "Tsetmc"
    )
}

/**
 * Maps Tsetmc Index DTO to domain MarketAsset.
 */
fun TsetmcIndexDto.toDomain(): MarketAsset {
    val currentValue = value ?: 0.0
    val pcp = changePercent ?: 0.0
    return MarketAsset(
        id = index ?: name ?: "",
        symbol = name ?: "",
        name = name ?: "",
        type = MarketAssetType.STOCK, // Indices are part of stock market
        price = currentValue,
        previousPrice = currentValue / (1 + (pcp / 100)),
        change = currentValue - (currentValue / (1 + (pcp / 100))),
        changePercent = pcp,
        timestamp = System.currentTimeMillis(),
        source = "Tsetmc"
    )
}
