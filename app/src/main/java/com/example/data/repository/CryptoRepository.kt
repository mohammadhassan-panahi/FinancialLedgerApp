package com.example.data.repository

import com.example.crypto.analysis.CandleStick
import com.example.data.local.CryptoAssetEntity
import com.example.data.local.CryptoDao
import com.example.data.local.CryptoInfoEntity
import com.example.data.remote.BinanceApiService
import com.example.data.remote.CmcCoinDto
import com.example.data.remote.CoinMarketCapApiService
import kotlinx.coroutines.flow.Flow

/**
 * Crypto market data — Phase 1 (Market Data + basic info) of the crypto analysis feature.
 * Deliberately scoped to what CoinMarketCap's free/Basic tier actually provides; see the
 * doc comment on CoinMarketCapApiService for what's explicitly OUT of scope here (on-chain,
 * security, developer activity, TVL/ecosystem, technical indicators) and would need separate
 * repositories/data sources layered in alongside this one later — this repository's job is
 * only to be the "MarketDataSource" piece of that eventual bigger picture, not to grow into
 * doing everything itself.
 *
 * Kept completely separate from PortfolioRepository/FinancialRepository: crypto assets here
 * are NOT part of the user's Rial portfolio holdings (no purchases/sales/cost-basis) — this
 * is read-only market/reference data, more like the existing StockDao watchlist pattern but
 * for global crypto rather than TSE symbols.
 *
 * If [apiKey] is blank, all refresh calls simply no-op and return false — same convention as
 * PortfolioRepository/FinancialRepository's proxy/API-key handling.
 */
class CryptoRepository(
    private val cryptoDao: CryptoDao,
    private val apiKey: String = "",
    private val apiService: CoinMarketCapApiService? = if (apiKey.isNotBlank()) CoinMarketCapApiService.create() else null,
    private val binanceService: BinanceApiService = BinanceApiService.create()
) {
    val allAssets: Flow<List<CryptoAssetEntity>> = cryptoDao.getAllAssets()
    val watchlist: Flow<List<CryptoAssetEntity>> = cryptoDao.getWatchlist()

    /**
     * Fetches historical OHLC data from Binance for technical analysis.
     * Maps the symbol (e.g., BTC) to a USDT pair (e.g., BTCUSDT).
     */
    suspend fun fetchHistory(symbol: String, interval: String = "1h"): Result<List<CandleStick>> {
        val binanceSymbol = if (symbol.contains("USDT")) symbol else "${symbol}USDT"
        return try {
            val response = binanceService.getKlines(binanceSymbol, interval)
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return Result.failure(IllegalStateException("خطا در دریافت تاریخچه قیمت از بایننس (HTTP ${response.code()})"))
            }

            val candles = body.mapNotNull { list ->
                try {
                    // Binance returns [OpenTime, Open, High, Low, Close, Volume, ...]
                    // Moshi/Retrofit with List<Any> often parses numbers as Double
                    val time = when (val t = list[0]) {
                        is Double -> t.toLong()
                        is Long -> t
                        is String -> t.toLong()
                        else -> 0L
                    }
                    CandleStick(
                        time = time,
                        open = (list[1] as String).toDouble(),
                        high = (list[2] as String).toDouble(),
                        low = (list[3] as String).toDouble(),
                        close = (list[4] as String).toDouble(),
                        volume = (list[5] as String).toDouble()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(candles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches the top [limit] coins by market cap and replaces the cached list. Costs
     * roughly 1 CMC credit per 200 coins requested — call this sparingly (e.g. on manual
     * refresh / screen open), not on a tight polling loop, to conserve the free tier's
     * monthly credit budget.
     */
    suspend fun refreshTopListings(limit: Int = 100): Result<Int> {
        val service = apiService ?: return Result.failure(IllegalStateException("کلید CoinMarketCap تنظیم نشده است"))
        return try {
            val response = service.getListings(apiKey = apiKey, start = 1, limit = limit, convert = "USD")
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return Result.failure(
                    IllegalStateException(body?.status?.errorMessage ?: "خطا در دریافت لیست ارزها (HTTP ${response.code()})")
                )
            }
            if (body.status.errorCode != 0) {
                return Result.failure(IllegalStateException(body.status.errorMessage ?: "خطای نامشخص از CoinMarketCap"))
            }
            val entities = body.data.map { it.toEntity() }
            // Preserve each coin's existing isInWatchlist flag rather than clobbering it.
            val withWatchlistPreserved = entities.map { entity ->
                val existing = cryptoDao.getBySymbol(entity.symbol)
                if (existing != null) entity.copy(isInWatchlist = existing.isInWatchlist) else entity
            }
            cryptoDao.insertAssets(withWatchlistPreserved)
            Result.success(withWatchlistPreserved.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Refreshes just the watchlisted symbols — cheaper than a full listings refresh. */
    suspend fun refreshWatchlistPrices(): Result<Int> {
        val service = apiService ?: return Result.failure(IllegalStateException("کلید CoinMarketCap تنظیم نشده است"))
        val watchlistSymbols = cryptoDao.getAllAssetsOnce().filter { it.isInWatchlist }.map { it.symbol }
        if (watchlistSymbols.isEmpty()) return Result.success(0)

        return try {
            val response = service.getQuotesBySymbol(apiKey = apiKey, symbols = watchlistSymbols.joinToString(","), convert = "USD")
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return Result.failure(
                    IllegalStateException(body?.status?.errorMessage ?: "خطا در دریافت قیمت‌های واچ‌لیست (HTTP ${response.code()})")
                )
            }
            if (body.status.errorCode != 0) {
                return Result.failure(IllegalStateException(body.status.errorMessage ?: "خطای نامشخص از CoinMarketCap"))
            }
            // Note: /quotes/latest by symbol can return MULTIPLE coins sharing the same
            // symbol (e.g. wrapped versions) as an array per key in some CMC responses;
            // our DTO models the common case (one coin per requested symbol key).
            val entities = body.data.values.map { it.toEntity().copy(isInWatchlist = true) }
            cryptoDao.insertAssets(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToWatchlist(symbol: String) = cryptoDao.setWatchlist(symbol, true)
    suspend fun removeFromWatchlist(symbol: String) = cryptoDao.setWatchlist(symbol, false)

    /**
     * Fetches static metadata (website/whitepaper/logo/description) for one coin and caches
     * it — this data changes rarely, so callers should check the cache (e.g. via
     * [getCachedInfo]) before calling this, rather than refetching on every screen visit.
     */
    suspend fun refreshInfo(cmcId: Int, symbol: String): Result<CryptoInfoEntity> {
        val service = apiService ?: return Result.failure(IllegalStateException("کلید CoinMarketCap تنظیم نشده است"))
        return try {
            val response = service.getInfoBySymbol(apiKey = apiKey, symbols = symbol)
            val body = response.body()
            val dto = body?.data?.values?.firstOrNull { it.id == cmcId } ?: body?.data?.values?.firstOrNull()
            if (!response.isSuccessful || dto == null) {
                return Result.failure(
                    IllegalStateException(body?.status?.errorMessage ?: "اطلاعات این ارز یافت نشد (HTTP ${response.code()})")
                )
            }
            val entity = CryptoInfoEntity(
                cmcId = dto.id,
                category = dto.category,
                description = dto.description,
                logoUrl = dto.logo,
                websiteUrl = dto.urls?.website?.firstOrNull(),
                whitepaperUrl = dto.urls?.technicalDoc?.firstOrNull(),
                explorerUrl = dto.urls?.explorer?.firstOrNull(),
                sourceCodeUrl = dto.urls?.sourceCode?.firstOrNull(),
                dateAdded = dto.dateAdded
            )
            cryptoDao.insertInfo(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCachedInfo(cmcId: Int): CryptoInfoEntity? = cryptoDao.getInfo(cmcId)

    /** Global market snapshot: total market cap, BTC/ETH dominance. Cheap call, 1 credit. */
    suspend fun fetchGlobalMetrics(): Result<GlobalMarketSnapshot> {
        val service = apiService ?: return Result.failure(IllegalStateException("کلید CoinMarketCap تنظیم نشده است"))
        return try {
            val response = service.getGlobalMetrics(apiKey = apiKey, convert = "USD")
            val body = response.body()
            val data = body?.data
            if (!response.isSuccessful || data == null) {
                return Result.failure(
                    IllegalStateException(body?.status?.errorMessage ?: "خطا در دریافت وضعیت کلی بازار (HTTP ${response.code()})")
                )
            }
            val usdQuote = data.quote?.get("USD")
            Result.success(
                GlobalMarketSnapshot(
                    totalMarketCapUsd = usdQuote?.totalMarketCap,
                    totalVolume24hUsd = usdQuote?.totalVolume24h,
                    btcDominance = data.btcDominance,
                    ethDominance = data.ethDominance,
                    activeCryptocurrencies = data.activeCryptocurrencies
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/** "Global Market" section of the requested feature list — total cap + BTC/ETH dominance. */
data class GlobalMarketSnapshot(
    val totalMarketCapUsd: Double?,
    val totalVolume24hUsd: Double?,
    val btcDominance: Double?,
    val ethDominance: Double?,
    val activeCryptocurrencies: Int?
)

private fun CmcCoinDto.toEntity(): CryptoAssetEntity {
    val usd = quote?.get("USD")
    return CryptoAssetEntity(
        cmcId = id,
        symbol = symbol,
        name = name,
        slug = slug,
        cmcRank = cmcRank,
        priceUsd = usd?.price,
        percentChange1h = usd?.percentChange1h,
        percentChange24h = usd?.percentChange24h,
        percentChange7d = usd?.percentChange7d,
        percentChange30d = usd?.percentChange30d,
        marketCapUsd = usd?.marketCap,
        fullyDilutedMarketCapUsd = usd?.fullyDilutedMarketCap,
        volume24hUsd = usd?.volume24h,
        volumeChange24h = usd?.volumeChange24h,
        circulatingSupply = circulatingSupply,
        totalSupply = totalSupply,
        maxSupply = maxSupply,
        infiniteSupply = infiniteSupply ?: false,
        platformName = platform?.name,
        tokenAddress = platform?.tokenAddress,
        tags = tags?.joinToString(","),
        isInWatchlist = false // caller decides whether to preserve/override this
    )
}
