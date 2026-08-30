package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Real Room migrations, replacing fallbackToDestructiveMigration() — every schema change from
 * here on MUST ship with a corresponding Migration(N, N+1) or existing users lose their data
 * on update. SQL below is hand-written to match the exact columns/types Room generates for
 * the entities in PortfolioEntities.kt at each version (see git history / entity comments for
 * what changed at each step). Kotlin enums (PortfolioAssetType, AlertDirection) are stored by
 * Room's built-in enum support as TEXT (the enum constant's name).
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `asset_purchases` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `assetType` TEXT NOT NULL,
                `assetCode` TEXT NOT NULL,
                `assetName` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `unitPriceRial` REAL NOT NULL,
                `totalPaidRial` REAL NOT NULL,
                `purchaseDate` INTEGER NOT NULL,
                `note` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_asset_purchases_assetCode` ON `asset_purchases` (`assetCode`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `market_index` (
                `indexCode` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `value` REAL NOT NULL,
                `changePercent` REAL NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`indexCode`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `stock_symbols` (
                `symbol` TEXT NOT NULL,
                `fullName` TEXT NOT NULL,
                `lastPriceRial` REAL NOT NULL,
                `changePercent` REAL NOT NULL,
                `isInWatchlist` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`symbol`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `price_alerts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `assetCode` TEXT NOT NULL,
                `assetName` TEXT NOT NULL,
                `targetPriceRial` REAL NOT NULL,
                `direction` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL,
                `lastTriggeredAt` INTEGER,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `stock_symbols` ADD COLUMN `buyPriceRial` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `stock_symbols` ADD COLUMN `sellPriceRial` REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `asset_sales` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `assetType` TEXT NOT NULL,
                `assetCode` TEXT NOT NULL,
                `assetName` TEXT NOT NULL,
                `quantitySold` REAL NOT NULL,
                `saleUnitPriceRial` REAL NOT NULL,
                `totalReceivedRial` REAL NOT NULL,
                `costBasisRial` REAL NOT NULL,
                `realizedPnlRial` REAL NOT NULL,
                `saleDate` INTEGER NOT NULL,
                `note` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_asset_sales_assetCode` ON `asset_sales` (`assetCode`)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `crypto_assets` (
                `cmcId` INTEGER PRIMARY KEY NOT NULL,
                `symbol` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `slug` TEXT,
                `cmcRank` INTEGER,
                `priceUsd` REAL,
                `percentChange1h` REAL,
                `percentChange24h` REAL,
                `percentChange7d` REAL,
                `percentChange30d` REAL,
                `marketCapUsd` REAL,
                `fullyDilutedMarketCapUsd` REAL,
                `volume24hUsd` REAL,
                `volumeChange24h` REAL,
                `circulatingSupply` REAL,
                `totalSupply` REAL,
                `maxSupply` REAL,
                `infiniteSupply` INTEGER NOT NULL DEFAULT 0,
                `platformName` TEXT,
                `tokenAddress` TEXT,
                `tags` TEXT,
                `isInWatchlist` INTEGER NOT NULL DEFAULT 0,
                `lastUpdated` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_crypto_assets_symbol` ON `crypto_assets` (`symbol`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `crypto_info` (
                `cmcId` INTEGER PRIMARY KEY NOT NULL,
                `category` TEXT,
                `description` TEXT,
                `logoUrl` TEXT,
                `websiteUrl` TEXT,
                `whitepaperUrl` TEXT,
                `explorerUrl` TEXT,
                `sourceCodeUrl` TEXT,
                `dateAdded` TEXT,
                `lastUpdated` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bank_accounts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `bankName` TEXT NOT NULL,
                `initialBalance` REAL NOT NULL,
                `currentBalance` REAL NOT NULL,
                `colorHex` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `accountId` INTEGER")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `debt_credits` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `personName` TEXT NOT NULL,
                `amountRial` REAL NOT NULL,
                `type` TEXT NOT NULL,
                `dueDate` INTEGER,
                `description` TEXT NOT NULL,
                `isSettled` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `amountRial` REAL NOT NULL,
                `type` TEXT NOT NULL,
                `dueDate` INTEGER NOT NULL,
                `isPaid` INTEGER NOT NULL,
                `note` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `financial_goals` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `targetAmountRial` REAL NOT NULL,
                `currentSavedRial` REAL NOT NULL,
                `deadline` INTEGER,
                `category` TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `initial_public_offerings` (
                `symbol` TEXT PRIMARY KEY NOT NULL,
                `companyName` TEXT NOT NULL,
                `ipoDate` TEXT NOT NULL,
                `maxShares` INTEGER NOT NULL,
                `maxPriceRial` REAL NOT NULL,
                `minPriceRial` REAL NOT NULL,
                `requiredLiquidityRial` REAL NOT NULL,
                `status` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `codal_notices` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `symbol` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `publishDate` TEXT NOT NULL,
                `link` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `isRead` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `vehicles` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `modelName` TEXT NOT NULL,
                `priceRial` REAL NOT NULL,
                `changePercent` REAL NOT NULL,
                `lastUpdate` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `real_estate` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `propertyName` TEXT NOT NULL,
                `valuationRial` REAL NOT NULL,
                `changePercent` REAL NOT NULL,
                `lastUpdate` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `risk_profiles` (
                `userId` TEXT PRIMARY KEY NOT NULL,
                `riskScore` INTEGER NOT NULL,
                `personalityType` TEXT NOT NULL,
                `lastAssessmentDate` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `investment_roadmaps` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `roadmapJson` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `social_posts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `authorName` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `assetCode` TEXT,
                `sentiment` TEXT,
                `likesCount` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `portfolio_snapshots` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `totalValueRial` REAL NOT NULL,
                `totalProfitLossRial` REAL NOT NULL,
                `goldPriceRial` REAL NOT NULL,
                `usdPriceRial` REAL NOT NULL,
                `stockIndexValue` REAL NOT NULL,
                `allocationByAssetJson` TEXT NOT NULL,
                `allocationByTypeJson` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `market_rates` ADD COLUMN `priceGlobal` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `market_rates` ADD COLUMN `currency` TEXT NOT NULL DEFAULT 'تومان'")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `social_posts` ")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `news_items` (
                `id` TEXT PRIMARY KEY NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT,
                `source` TEXT NOT NULL,
                `url` TEXT NOT NULL,
                `imageUrl` TEXT,
                `publishedAt` INTEGER NOT NULL,
                `category` TEXT NOT NULL,
                `importance` TEXT NOT NULL,
                `sentiment` TEXT NOT NULL,
                `aiSummary` TEXT,
                `relatedAssets` TEXT
            )
            """.trimIndent()
        )
    }
}

/** All migrations in order — pass this whole array to `.addMigrations(...)`. */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
    MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
    MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17
)
