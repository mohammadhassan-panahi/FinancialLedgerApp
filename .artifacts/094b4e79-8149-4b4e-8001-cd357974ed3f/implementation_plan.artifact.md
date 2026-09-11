# Fix BigDecimal and Double Type Mismatches

The project is currently failing to build due to numerous type mismatches between `BigDecimal` and `Double` (or `Int`). These issues primarily occur in the UI layer where market data (stored as `BigDecimal` for precision) is being passed to calculation utilities or chart components that expect `Double`, or vice versa.

## User Review Required

> [!IMPORTANT]
> Some calculations in the UI were using `Double` for financial values. To maintain consistency and precision, I will convert these to `BigDecimal` where appropriate, or use `.toDouble()` when passing values to purely visual components (like charts).

## Proposed Changes

### UI Screens

#### [MODIFY] [PortfolioHomeScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/PortfolioHomeScreen.kt)
- Convert `BigDecimal` fields to `Double` when creating `BenchmarkPoint` for the performance chart. (Already partially fixed, will ensure completeness).

#### [MODIFY] [ScenarioScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/ScenarioScreen.kt)
- Update `portfolioLegs` to store `Double` values or ensure comparison with `BigDecimal.ZERO`.
- Fix the `legs` list type mismatch to match `GoldMarketFormulas.calculateScenario` expectations.
- Use `BigDecimal.ZERO` for comparisons.

#### [MODIFY] [ScenarioSimulatorScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/ScenarioSimulatorScreen.kt)
- Fix division of `BigDecimal` by `Double`.
- Ensure consistent use of `Double` for simulation variables or `BigDecimal` for base wealth.

#### [MODIFY] [SimpleInterestScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/SimpleInterestScreen.kt)
- Convert parsed `Double` inputs to `BigDecimal` before calling `FinancialFormulas.calculateSimpleInterest`.

#### [MODIFY] [StockMarketScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/StockMarketScreen.kt)
- Fix `MarketBreadth` statistics calculation (average of `BigDecimal`).
- Ensure `Int` to `BigDecimal` conversion for gainer/loser counts if required by the model.

#### [MODIFY] [SwapMarketScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/SwapMarketScreen.kt)
- Convert `Double` calculation results to `BigDecimal` before calling `viewModel.addTransaction`.

#### [MODIFY] [TransferScreen.kt](file:///C:/Users/PANAHI/Desktop/android%20appp/portfo%20app%20android%20v4/finapp/app/src/main/java/com/example/ui/screens/TransferScreen.kt)
- Fix comparison between `Double` amount and `BigDecimal` balance.
- Convert `Double` amount to `BigDecimal` for transaction recording.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify all compilation errors are resolved.

### Manual Verification
- Verify that the Scenario Simulator and Interest Calculators still produce correct results in the UI.
