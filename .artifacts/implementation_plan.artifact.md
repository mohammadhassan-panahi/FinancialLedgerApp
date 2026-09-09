# DARA — Phase 1: Bug Fix + Data Integrity

This phase focuses on ensuring financial precision and data integrity across the application, adhering to Sections 9 and 10 of the Master Development Prompt.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [TypeConverters.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/TypeConverters.kt)
Add Room TypeConverters for `BigDecimal` to ensure precision in database storage.

#### [MODIFY] Entities
Migrate all `Double` fields representing currency/price to `BigDecimal` in the following files:
- [PortfolioEntities.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/PortfolioEntities.kt)
- [TransactionEntity.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/TransactionEntity.kt)
- [CryptoEntities.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/CryptoEntities.kt)
- [BankAccountEntity.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/BankAccountEntity.kt)
- [DebtCreditEntities.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/DebtCreditEntities.kt)
- [ReminderEntities.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/ReminderEntities.kt)
- [GoalEntities.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/GoalEntities.kt)

#### [MODIFY] [Migrations.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/data/local/Migrations.kt)
Add a new migration (17 to 18) to handle the database schema change if Room detects type differences (though TypeConverters might handle the translation from Double in DB to BigDecimal in Code, it's safer to ensure schema matches).

### [Navigation & UI]

#### [MODIFY] [NavGraph.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/navigation/NavGraph.kt)
- Remove hardcoded `usdRateToman = 65000.0`.
- Inject `usdPriceToman` from `PortfolioViewModel` into `MarketScannerScreen`.

## Verification Plan

### Automated Tests
- Run `testDebugUnitTest` to ensure existing logic still works with `BigDecimal`.
- Add specific `FinancialCalculationTest` for `BigDecimal` precision.

### Manual Verification
- Verify that monetary values display correctly in dashboard and portfolio screens.
- Test adding new assets and check if values are preserved with precision.
