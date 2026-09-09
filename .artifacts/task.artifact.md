# DARA Development Tasks

## Phase 0: Audit + Baseline [DONE]
- [x] Fix compilation errors in `NavGraph.kt` and `CurrencyConverterScreen.kt`
- [x] Establish successful baseline build (`assembleDebug`)
- [x] Run baseline unit tests (`testDebugUnitTest`)

## Phase 1: Bug Fix + Data Integrity
- [/] Migrate monetary fields from `Double` to `BigDecimal` (or scaled `Long`)
    - [ ] Add `BigDecimal` TypeConverters for Room
    - [ ] Update `AssetPurchaseEntity`
    - [ ] Update `AssetSaleEntity`
    - [ ] Update `TransactionEntity`
    - [ ] Update `BankAccountEntity`
    - [ ] Update `DebtCreditEntity`
    - [ ] Update `ReminderEntity`
    - [ ] Update `GoalEntity`
    - [ ] Update `StockSymbolEntity`
    - [ ] Update `CryptoAssetEntity`
- [ ] Ensure Date Integrity (Section 10)
    - [ ] Audit all `System.currentTimeMillis()` defaults in Entities
    - [ ] Ensure UI passes user-selected dates to Repositories
- [ ] Remove Hardcoded Market Data (Section 14)
    - [ ] Fix `usdRateToman` in `NavGraph.kt`
    - [ ] Audit other UI components for hardcoded values
- [ ] Refactor NavGraph & Navigation Cleanup
    - [ ] Remove unreachable/redundant routes

## Phase 2: Crypto Market Upgrade
- [ ] Improve CMC integration
- [ ] Add Binance Public Data for Charts
- [ ] Implement Market Dashboard (Cap, Volume, BTC Dominance)

... (other phases to follow)
