package com.example.data.remote

import com.example.domain.model.MarketAsset
import com.example.domain.model.MarketAssetType
import com.example.util.safeDiv
import java.math.BigDecimal

/**
 * Maps BrsApi Rate DTO to domain MarketAsset.
 */
fun BrsApiRateDto.toDomain(type: MarketAssetType): MarketAsset {
    val currentPrice = BigDecimal.valueOf(price)
    val cp = BigDecimal.valueOf(changePercent)
    val divisor = BigDecimal.ONE.add(cp.safeDiv(BigDecimal("100")))
    val previousPrice = currentPrice.safeDiv(divisor)
    
    return MarketAsset(
        id = symbol,
        symbol = symbol,
        name = name,
        type = type,
        price = currentPrice,
        previousPrice = previousPrice,
        change = currentPrice.subtract(previousPrice),
        changePercent = cp,
        timestamp = timeUnix?.times(1000) ?: System.currentTimeMillis(),
        source = "BrsApi"
    )
}

/**
 * Maps BrsApi Crypto DTO to domain MarketAsset.
 */
fun BrsApiCryptoDto.toDomain(): MarketAsset {
    val currentPrice = price.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val cp = BigDecimal.valueOf(changePercent)
    val divisor = BigDecimal.ONE.add(cp.safeDiv(BigDecimal("100")))
    val previousPrice = currentPrice.safeDiv(divisor)

    return MarketAsset(
        id = symbol,
        symbol = symbol,
        name = name,
        type = MarketAssetType.CRYPTO,
        price = currentPrice,
        previousPrice = previousPrice,
        change = currentPrice.subtract(previousPrice),
        changePercent = cp,
        timestamp = timeUnix?.times(1000) ?: System.currentTimeMillis(),
        source = "BrsApi"
    )
}

/**
 * Maps Tsetmc Symbol DTO to domain MarketAsset.
 */
fun TsetmcSymbolDto.toDomain(): MarketAsset {
    val currentPrice = closingPrice?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
    val cp = changePercent?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
    val divisor = BigDecimal.ONE.add(cp.safeDiv(BigDecimal("100")))
    val previousPrice = currentPrice.safeDiv(divisor)

    return MarketAsset(
        id = symbol ?: "",
        symbol = symbol ?: "",
        name = fullName ?: "",
        type = MarketAssetType.STOCK,
        price = currentPrice,
        previousPrice = previousPrice,
        change = currentPrice.subtract(previousPrice),
        changePercent = cp,
        timestamp = System.currentTimeMillis(),
        source = "Tsetmc"
    )
}

/**
 * Maps Tsetmc Index DTO to domain MarketAsset.
 */
fun TsetmcIndexDto.toDomain(): MarketAsset {
    val currentPrice = value?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
    val cp = changePercent?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
    val divisor = BigDecimal.ONE.add(cp.safeDiv(BigDecimal("100")))
    val previousPrice = currentPrice.safeDiv(divisor)

    return MarketAsset(
        id = index ?: name ?: "",
        symbol = name ?: "",
        name = name ?: "",
        type = MarketAssetType.STOCK, // Indices are part of stock market
        price = currentPrice,
        previousPrice = previousPrice,
        change = currentPrice.subtract(previousPrice),
        changePercent = cp,
        timestamp = System.currentTimeMillis(),
        source = "Tsetmc"
    )
}
