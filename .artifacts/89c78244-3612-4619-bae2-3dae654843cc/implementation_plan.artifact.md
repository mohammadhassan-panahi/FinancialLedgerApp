# Implementation Plan - Computational Toolkit Fix & Regression Cleanup

This plan addresses the critical regression in the "Computational Toolkit" (Calculators) and fixes several screens that currently use fake/mock data instead of real portfolio/market values. It also improves the news aggregation system.

## User Review Required

> [!IMPORTANT]
> Several screens were found to have hardcoded UI values (e.g., gold prices, AI scores, portfolio simulations). This plan will wire them to the real data sources (Room database, Market API, and Gemini AI).

> [!WARNING]
> The `CalculatorsHubScreen` currently has broken logic (static results). I will re-implement the calculation logic and link it to dedicated calculator screens that already exist in the codebase but are currently unreachable.

## Proposed Changes

### [Navigation & Routing]

#### [MODIFY] [NavGraph.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/navigation/NavGraph.kt)
- Add missing routes to the `Screen` object: `CurrencyConverter`, `CompoundInterest`, `SimpleInterest`, `LoanCalculator`, `GoldWage`, `GoldBubble`.
- Wire these new routes in the `NavHost`.

### [Computational Toolkit]

#### [MODIFY] [CalculatorsHubScreen.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/ui/screens/CalculatorsHubScreen.kt)
- Re-implement the embedded Gold Calculator logic using real-time market rates from `PortfolioViewModel`.
- Wire the "Currency Converter", "Crypto to Toman", and "Compound Interest" cards to navigate to their respective screens.

#### [MODIFY] [CalculatorViewModel.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt)
- Add calculation logic (e.g., interest formulas) if needed to keep the UI clean, or ensure the UI can handle them efficiently.

### [Data Wiring (Fixing "Fake Data")]

#### [MODIFY] [ScenarioSimulatorScreen.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/ui/screens/ScenarioSimulatorScreen.kt)
- Replace mock `baseWorth` and allocations with real data from `PortfolioViewModel`.

#### [MODIFY] [CryptoIntelligenceScreen.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/ui/screens/CryptoIntelligenceScreen.kt)
- Use real crypto assets and prices instead of hardcoded BTC mock.
- (Optional) Implement a basic AI scoring helper or wire to `AiAnalysisViewModel`.

#### [MODIFY] [NewsDetailScreen.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/ui/screens/news/NewsDetailScreen.kt)
- Remove hardcoded AI summary bullets.
- Use `news.aiSummary` and potentially other `NewsEntity` fields.

#### [MODIFY] [AssetComparisonScreen.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/ui/screens/AssetComparisonScreen.kt)
- Use real market rates for comparison instead of hardcoded values.

### [News & Infrastructure]

#### [MODIFY] [RssService.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/data/remote/RssService.kt)
- Add `User-Agent` header to avoid being blocked by RSS providers.

#### [MODIFY] [NewsRepository.kt](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/src/main/java/com/example/data/repository/NewsRepository.kt)
- Improve error handling in `aggregateAndSave` to propagate feed-specific failures instead of silent failures.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/PANAHI/Desktop/android appp/portfo app android v4/finapp/app/build.gradle.kts)
- Add documentation comments for `NEWS_API_KEY` clarifying its usage (CryptoPanic/NewsAPI).

## Verification Plan

### Automated Tests
- Build the project to ensure all new routes are resolved: `./gradlew assembleDebug`
- (Optional) Run unit tests for calculation logic.

### Manual Verification
- Open "Toolkit" and verify:
    - Gold calculator updates result based on weight and real market price.
    - Clicking "Currency Converter" opens the converter screen.
    - Clicking "Compound Interest" opens the compound interest screen.
- Open "Scenario Simulator" and verify the "Current Wealth" matches the Dashboard.
- Open "Crypto Intelligence" and verify prices match real market data.
- Refresh news and check Logcat for any RSS fetch errors.
