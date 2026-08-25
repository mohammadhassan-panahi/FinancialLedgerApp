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

/** All migrations in order — pass this whole array to `.addMigrations(...)`. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
