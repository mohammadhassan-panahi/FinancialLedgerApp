package com.example.data.repository

import com.example.data.local.BankAccountDao
import com.example.data.local.BankAccountEntity
import com.example.data.local.MarketDao
import com.example.data.local.MarketRateEntity
import com.example.data.local.MutualFundEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.remote.MarketApiService
import kotlinx.coroutines.flow.Flow

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val bankAccountDao: BankAccountDao,
    private val marketDao: MarketDao,
    private val apiKey: String = "",
    private val marketApiService: MarketApiService? = if (apiKey.isNotBlank()) MarketApiService.create() else null
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpenses()
    val marketRates: Flow<List<MarketRateEntity>> = marketDao.getAllMarketRates()
    val mutualFunds: Flow<List<MutualFundEntity>> = marketDao.getAllMutualFunds()

    val allBankAccounts: Flow<List<BankAccountEntity>> = bankAccountDao.getAllAccounts()
    val totalLiquidity: Flow<Double?> = bankAccountDao.getTotalLiquidity()

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
        // Automatically update bank account balance if accountId is provided
        transaction.accountId?.let { accountId ->
            val change = when (transaction.type) {
                TransactionType.DEPOSIT -> transaction.amount
                TransactionType.EXPENSE -> -transaction.amount
                else -> 0.0
            }
            if (change != 0.0) {
                bankAccountDao.updateBalance(accountId, change)
            }
        }
    }

    suspend fun removeTransaction(id: Long) {
        // Note: Reverting the balance on delete is safer, but requires fetching the txn first
        transactionDao.deleteTransaction(id)
    }

    suspend fun addBankAccount(account: BankAccountEntity) {
        bankAccountDao.insertAccount(account)
    }

    suspend fun updateBankAccount(account: BankAccountEntity) {
        bankAccountDao.updateAccount(account)
    }

    suspend fun deleteBankAccount(account: BankAccountEntity) {
        bankAccountDao.deleteAccount(account)
    }

    /**
     * Fetches live gold/currency rates from the BrsApi.ir free webservice and stores them in
     * the local (encrypted) database. If the network call fails, or no API key is configured,
     * it falls back to the last-known local rates instead of overwriting them with fake data.
     *
     * Returns true if a live fetch succeeded, false if the offline fallback was used.
     *
     * Mutual funds are NOT part of this API (BrsApi's free tier only covers gold, currencies
     * and crypto) so fund data stays local/manual — see saveTodayMarketRates().
     */
    suspend fun refreshMarketData(): Boolean {
        val service = marketApiService
        if (service == null) {
            // No API key configured (e.g. .env not set up) -> keep existing/offline rates as-is.
            ensureDefaultsSeeded()
            return false
        }

        return try {
            val response = service.getGoldCurrency(apiKey)
            val body = response.body()

            if (response.isSuccessful && body != null && body.successful != false) {
                // MarketRateEntity.priceToman assumes Toman; BrsApi's "gold" array includes one
                // USD-denominated row (XAUUSD / "انس طلا") which we skip here to avoid storing
                // a dollar price under a field named priceToman. Everything else in "gold" and
                // "currency" is already quoted in Toman.
                val liveRates = (body.gold + body.currency)
                    .filter { it.unit == "تومان" }
                    .map { dto ->
                        MarketRateEntity(
                            assetCode = dto.symbol,
                            name = dto.name,
                            priceToman = dto.price,
                            changePercent = dto.changePercent,
                            isOfflineRate = false
                        )
                    }
                if (liveRates.isNotEmpty()) {
                    marketDao.insertMarketRates(liveRates)
                }
                ensureDefaultFundsSeeded()
                true
            } else {
                // e.g. {"successful":false,"status":"unauthorized","message_error":"..."} -> bad/missing key
                ensureDefaultsSeeded()
                false
            }
        } catch (e: Exception) {
            // Network failure (no internet, timeout, etc.) -> keep last-known local rates.
            ensureDefaultsSeeded()
            false
        }
    }

    /**
     * Saves baseline offline market rates into the database, but only if nothing is stored yet
     * (so this never clobbers real live rates that were already fetched successfully).
     */
    suspend fun saveTodayMarketRates() {
        ensureDefaultsSeeded()
    }

    private suspend fun ensureDefaultsSeeded() {
        if (marketDao.getMarketRateCount() == 0) {
            val defaultRates = listOf(
                MarketRateEntity("USD", "دلار آمریکا", 61500.0, 0.45, isOfflineRate = true),
                MarketRateEntity("GOLD_18K", "طلا ۱۸ عیار (گرم)", 3650000.0, 1.2, isOfflineRate = true),
                MarketRateEntity("AZADI", "سکه امامی", 42800000.0, -0.3, isOfflineRate = true),
                MarketRateEntity("EUR", "یورو", 66200.0, 0.15, isOfflineRate = true)
            )
            marketDao.insertMarketRates(defaultRates)
        }
        ensureDefaultFundsSeeded()
    }

    private suspend fun ensureDefaultFundsSeeded() {
        if (marketDao.getMutualFundCount() == 0) {
            val defaultFunds = listOf(
                MutualFundEntity("FARABI", "صندوق اکسیر فارابی", 2450000.0, 24.5, "متوسط", "کارگزاری فارابی"),
                MutualFundEntity("MOFID", "صندوق پیشتاز مفید", 1890000.0, 28.1, "پرریسک", "کارگزاری مفید"),
                MutualFundEntity("ETEMAD", "صندوق اعتماد ملی", 3120000.0, 21.0, "کم‌ریسک", "سرمایه‌گذاری اعتماد")
            )
            marketDao.insertMutualFunds(defaultFunds)
        }
    }
}
