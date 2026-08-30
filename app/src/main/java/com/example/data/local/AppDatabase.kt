package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.security.DatabasePassphraseProvider
import com.example.BuildConfig
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        TransactionEntity::class,
        MarketRateEntity::class,
        MutualFundEntity::class,
        CalculationHistoryEntity::class,
        AssetPurchaseEntity::class,
        AssetSaleEntity::class,
        StockSymbolEntity::class,
        MarketIndexEntity::class,
        PriceAlertEntity::class,
        CryptoAssetEntity::class,
        CryptoInfoEntity::class,
        BankAccountEntity::class,
        DebtCreditEntity::class,
        ReminderEntity::class,
        GoalEntity::class,
        IpoEntity::class,
        CodalEntity::class,
        VehicleEntity::class,
        RealEstateEntity::class,
        RiskProfileEntity::class,
        InvestmentRoadmapEntity::class,
        NewsEntity::class,
        PortfolioSnapshotEntity::class
    ],
    version = 17,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun marketDao(): MarketDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun assetPurchaseDao(): AssetPurchaseDao
    abstract fun assetSaleDao(): AssetSaleDao
    abstract fun stockDao(): StockDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun cryptoDao(): CryptoDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun debtCreditDao(): DebtCreditDao
    abstract fun reminderDao(): ReminderDao
    abstract fun goalDao(): GoalDao
    abstract fun bourseDao(): BourseDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun realEstateDao(): RealEstateDao
    abstract fun nexFinDao(): NexFinDao
    abstract fun portfolioSnapshotDao(): PortfolioSnapshotDao
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                System.loadLibrary("sqlcipher")
                // SECURITY FIX: passphrase is now generated randomly per-install and stored
                // via Android Keystore-backed EncryptedSharedPreferences instead of being a
                // hardcoded plaintext constant in source. See DatabasePassphraseProvider.
                val passphrase = DatabasePassphraseProvider.getOrCreatePassphrase(context.applicationContext)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "financial_ledger_encrypted.db"
                )
                    .openHelperFactory(factory)
                    .addMigrations(*ALL_MIGRATIONS)
                    // Deliberately NO fallbackToDestructiveMigration(): every future schema
                    // change MUST add a Migration(N, N+1) to Migrations.kt, or the app will
                    // crash loudly on the missing-migration path instead of silently wiping
                    // the person's financial data — fail loud beats fail silent here.
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate database in background thread on creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    prepopulateDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(db: AppDatabase) {
            // Demo data is useful during development, but must never contaminate a
            // production user's financial ledger.
            if (!BuildConfig.DEBUG) return

            val transactionDao = db.transactionDao()
            val marketDao = db.marketDao()

            if (transactionDao.getTransactionCount() == 0) {
                val sampleTransactions = listOf(
                    TransactionEntity(
                        title = "واریز سرمایه اولیه دفتر",
                        amount = 150000000.0,
                        type = TransactionType.DEPOSIT,
                        category = "واریز درآمد شخصی",
                        note = "موجود اولیه دفتر محاسبات مالی"
                    ),
                    TransactionEntity(
                        title = "خرید واحدهای صندوق اکسیر فارابی",
                        amount = 30000000.0,
                        type = TransactionType.TRANSFER,
                        category = "انتقال به صندوق NAV",
                        note = "سرمایه‌گذاری در صندوق درآمد ثابت"
                    ),
                    TransactionEntity(
                        title = "تبدیل ریال به طلای ۱۸ عیار",
                        amount = 25000000.0,
                        type = TransactionType.SWAP,
                        category = "تبدیل دارایی",
                        note = "خرید طلای آب‌شده جهت حفظ ارزش"
                    ),
                    TransactionEntity(
                        title = "کارمزد معاملات و هزینه‌های جاری",
                        amount = 1200000.0,
                        type = TransactionType.EXPENSE,
                        category = "هزینه‌های عملیاتی",
                        note = "کارمزد کارگزاری و خدمات مالی"
                    )
                )
                transactionDao.insertTransactions(sampleTransactions)
            }

            if (marketDao.getMarketRateCount() == 0) {
                val defaultRates = listOf(
                    MarketRateEntity("USD", "دلار آمریکا", 61850.0, changePercent = 0.65, isOfflineRate = true),
                    MarketRateEntity("GOLD_18K", "طلا ۱۸ عیار (گرم)", 3685000.0, changePercent = 1.45, isOfflineRate = true),
                    MarketRateEntity("AZADI", "سکه امامی", 43100000.0, changePercent = 0.8, isOfflineRate = true),
                    MarketRateEntity("EUR", "یورو", 66550.0, changePercent = 0.35, isOfflineRate = true)
                )
                marketDao.insertMarketRates(defaultRates)
            }

            if (marketDao.getMutualFundCount() == 0) {
                val defaultFunds = listOf(
                    MutualFundEntity("FARABI", "صندوق اکسیر فارابی", 2480000.0, 25.2, "متوسط", "کارگزاری فارابی"),
                    MutualFundEntity("MOFID", "صندوق پیشتاز مفید", 1920000.0, 29.4, "پرریسک", "کارگزاری مفید"),
                    MutualFundEntity("ETEMAD", "صندوق اعتماد ملی", 3150000.0, 21.8, "کم‌ریسک", "سرمایه‌گذاری اعتماد")
                )
                marketDao.insertMutualFunds(defaultFunds)
            }

            val vehicleDao = db.vehicleDao()
            if (vehicleDao.getVehicleCount() == 0) {
                val defaultVehicles = listOf(
                    VehicleEntity(modelName = "پژو ۲۰۷ MC", priceRial = 9850000000.0, changePercent = 1.2),
                    VehicleEntity(modelName = "تارا اتوماتیک V4", priceRial = 11200000000.0, changePercent = 0.85),
                    VehicleEntity(modelName = "هایما S7 پلاس", priceRial = 18500000000.0, changePercent = 2.1)
                )
                vehicleDao.insertVehicles(defaultVehicles)
            }

            val realEstateDao = db.realEstateDao()
            if (realEstateDao.getPropertyCount() == 0) {
                val defaultProperties = listOf(
                    RealEstateEntity(propertyName = "آپارتمان مسکونی (تهران)", valuationRial = 45000000000.0),
                    RealEstateEntity(propertyName = "باغ مسکونی (دماوند)", valuationRial = 28000000000.0)
                )
                defaultProperties.forEach { realEstateDao.insertProperty(it) }
            }
        }
    }
}
