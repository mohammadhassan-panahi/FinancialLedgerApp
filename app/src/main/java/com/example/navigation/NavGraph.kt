package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.data.repository.UserPreferencesRepository
import com.example.security.BiometricAuthManager
import com.example.security.PinManager
import com.example.domain.model.Holding
import com.example.ui.dashboard.DaraDashboardScreen
import com.example.ui.dashboard.MarketScannerScreen
import com.example.ui.dashboard.MarketScannerViewModel
import com.example.ui.screens.news.NewsHubScreen
import com.example.ui.tools.ToolsScreen
import com.example.ui.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch

/** Central registry of every route in the app. */
object Screen {
    const val Onboarding = "onboarding"
    const val PinEntry = "pin_entry"
    const val Dashboard = "dashboard"
    const val NewsDetail = "news_detail"
    const val Market = "market"
    const val Portfolio = "portfolio"
    const val NewsHub = "news_hub"
    const val AiMentor = "ai_mentor"
    const val Tools = "tools"
    const val MarketScanner = "market_scanner"
    const val RiskAssessment = "risk_assessment"
    const val InvestmentRoadmap = "investment_roadmap"
    const val InflationCalculator = "inflation_calculator"
    const val ScenarioSimulator = "scenario_simulator"
    const val FinancialHealth = "financial_health"
    const val SmartAlerts = "smart_alerts"
    const val GlobalSearch = "global_search"
    const val AssetComparison = "asset_comparison"
    const val PortfolioReport = "portfolio_report"
    const val CryptoIntelligence = "crypto_intelligence"
    const val AddAssetForm = "add_asset_form"

    // Previously unreachable screens — now wired in.
    const val AddPurchase = "add_purchase"
    const val CalculatorsHub = "calculators_hub"
    const val BankAccounts = "bank_accounts"
    const val DebtCredits = "debt_credits"
    const val Reminders = "reminders"
    const val Goals = "goals"
    const val MutualFunds = "mutual_funds"
    const val OcrScanner = "ocr_scanner"
    const val Settings = "settings"
    const val PinSetup = "pin_setup"
    const val BiometricEnable = "biometric_enable"

    // Toolkit detailed screens
    const val CurrencyConverter = "currency_converter"
    const val CompoundInterest = "compound_interest"
    const val SimpleInterest = "simple_interest"
    const val LoanCalculator = "loan_calculator"
    const val GoldWage = "gold_wage"
    const val GoldBubble = "gold_bubble"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: com.example.ui.viewmodel.PortfolioViewModel,
    cryptoViewModel: CryptoViewModel,
    calculatorViewModel: com.example.ui.viewmodel.CalculatorViewModel,
    aiAnalysisViewModel: com.example.ui.viewmodel.AiAnalysisViewModel,
    riskAssessmentViewModel: com.example.ui.viewmodel.RiskAssessmentViewModel,
    settingsViewModel: com.example.ui.viewmodel.SettingsViewModel,
    newsViewModel: com.example.ui.viewmodel.NewsViewModel,
    marketScannerViewModel: MarketScannerViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    biometricAuthManager: BiometricAuthManager,
    pinManager: PinManager,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard,
        modifier = modifier
    ) {
        composable(Screen.Onboarding) {
            com.example.ui.screens.OnboardingScreen(
                onFinishOnboarding = {
                    scope.launch {
                        userPreferencesRepository.setOnboardingCompleted(true)
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Onboarding) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.PinEntry) {
            com.example.ui.screens.PinEntryScreen(
                biometricEnabled = pinManager.isBiometricEnabled(),
                onVerifyPin = { pinManager.verifyPin(it) },
                onUnlocked = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.PinEntry) { inclusive = true }
                    }
                },
                onBiometricRequested = {
                    biometricAuthManager.authenticate(
                        onSuccess = {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo(Screen.PinEntry) { inclusive = true }
                            }
                        },
                        onError = { /* Handle error */ }
                    )
                }
            )
        }

        composable(Screen.Dashboard) {
            DaraDashboardScreen(
                viewModel = viewModel,
                onNavigateToScanner = { navController.navigate(Screen.MarketScanner) },
                onNavigateToMarket = { navController.navigate(Screen.Market) },
                onNavigateToSearch = { navController.navigate(Screen.GlobalSearch) },
                onNavigateToHealth = { navController.navigate(Screen.FinancialHealth) },
                onNavigateToAlerts = { navController.navigate(Screen.SmartAlerts) }
            )
        }

        composable(Screen.GlobalSearch) {
            com.example.ui.screens.GlobalSearchScreen(
                onBack = { navController.popBackStack() },
                onAssetClick = { navController.navigate(Screen.Market) }
            )
        }

        composable(Screen.Market) {
            com.example.ui.screens.MarketHubScreen(
                portfolioViewModel = viewModel,
                cryptoViewModel = cryptoViewModel,
                onNavigateToIntelligence = { navController.navigate(Screen.CryptoIntelligence) }
            )
        }

        composable(Screen.FinancialHealth) {
            com.example.ui.screens.FinancialHealthScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAi = { navController.navigate(Screen.AiMentor) },
                onNavigateToSimulator = { navController.navigate(Screen.ScenarioSimulator) }
            )
        }

        composable(Screen.ScenarioSimulator) {
            com.example.ui.screens.ScenarioSimulatorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SmartAlerts) {
            com.example.ui.screens.SmartAlertsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AssetComparison) {
            com.example.ui.screens.AssetComparisonScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PortfolioReport) {
            com.example.ui.screens.PortfolioReportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CryptoIntelligence) {
            com.example.ui.screens.CryptoIntelligenceScreen(
                viewModel = cryptoViewModel,
                onBack = { navController.popBackStack() },
                onAssetClick = { /* Handle asset click */ }
            )
        }

        composable(Screen.Portfolio) {
            com.example.ui.screens.PortfolioHomeScreen(
                viewModel = viewModel,
                onExportRequested = { navController.navigate(Screen.PortfolioReport) },
                onImportRequested = onImportRequested,
                onOpenCalculators = { navController.navigate(Screen.CalculatorsHub) },
                onOpenBankAccounts = { navController.navigate(Screen.BankAccounts) },
                onOpenDebtCredits = { navController.navigate(Screen.DebtCredits) },
                onOpenReminders = { navController.navigate(Screen.Reminders) },
                onOpenGoals = { navController.navigate(Screen.Goals) },
                onOpenMutualFunds = { navController.navigate(Screen.MutualFunds) },
                onOpenAiAnalysis = { navController.navigate(Screen.AiMentor) },
                onOpenOcrScanner = { navController.navigate(Screen.OcrScanner) },
                onOpenAddPurchase = { navController.navigate(Screen.AddPurchase) },
                onOpenSettings = { navController.navigate(Screen.Settings) }
            )
        }

        composable(Screen.AiMentor) {
            com.example.ui.screens.AiAnalysisScreen(
                viewModel = aiAnalysisViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NewsHub) {
            NewsHubScreen(
                viewModel = newsViewModel,
                onNewsClick = { news ->
                    newsViewModel.selectNews(news)
                    navController.navigate(Screen.NewsDetail)
                }
            )
        }

        composable(Screen.NewsDetail) {
            val news by newsViewModel.selectedNews.collectAsStateWithLifecycle()
            news?.let {
                com.example.ui.screens.news.NewsDetailScreen(
                    news = it,
                    onBack = { navController.popBackStack() },
                    onChatWithAi = { navController.navigate(Screen.AiMentor) }
                )
            }
        }

        composable(Screen.Tools) {
            ToolsScreen(
                onOpenCalculators = { navController.navigate(Screen.CalculatorsHub) },
                onOpenRiskAssessment = { navController.navigate(Screen.RiskAssessment) },
                onOpenOcrScanner = { navController.navigate(Screen.OcrScanner) },
                onOpenInvestmentRoadmap = { navController.navigate(Screen.InvestmentRoadmap) },
                onOpenFinancialHealth = { navController.navigate(Screen.FinancialHealth) },
                onOpenScenarioSimulator = { navController.navigate(Screen.ScenarioSimulator) },
                onOpenAssetComparison = { navController.navigate(Screen.AssetComparison) }
            )
        }

        composable(Screen.MarketScanner) {
            val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
            val usdRateToman = 65000.0 // Simplified for IDE stability
            MarketScannerScreen(
                viewModel = marketScannerViewModel,
                usdRateToman = usdRateToman
            )
        }

        composable(Screen.RiskAssessment) {
            com.example.ui.screens.RiskAssessmentScreen(
                viewModel = riskAssessmentViewModel,
                onFinished = { navController.popBackStack() }
            )
        }

        composable(Screen.InvestmentRoadmap) {
            com.example.ui.screens.InvestmentRoadmapScreen(
                viewModel = aiAnalysisViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.InflationCalculator) {
            com.example.ui.screens.InflationCalculatorScreen(viewModel = viewModel)
        }

        composable(Screen.AddPurchase) {
            com.example.ui.screens.AddPurchaseScreen(
                viewModel = viewModel,
                onNextStep = { type ->
                    navController.navigate("${Screen.AddAssetForm}/${type.name}")
                }
            )
        }

        composable(
            route = "${Screen.AddAssetForm}/{assetType}",
            arguments = listOf(androidx.navigation.navArgument("assetType") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("assetType")
            val assetType = try { com.example.data.local.PortfolioAssetType.valueOf(typeStr ?: "") } catch(e: Exception) { com.example.data.local.PortfolioAssetType.CASH }
            
            com.example.ui.screens.AddAssetFormScreen(
                assetType = assetType,
                onBack = { navController.popBackStack() },
                onSubmit = { name, qty, price, date ->
                    viewModel.addPurchase(assetType, name, name, qty, price, System.currentTimeMillis(), "Manual")
                    navController.navigate(Screen.Portfolio) {
                        popUpTo(Screen.Dashboard) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.CalculatorsHub) {
            val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
            
            com.example.ui.screens.CalculatorsHubScreen(
                viewModel = calculatorViewModel,
                goldPriceToman = 3500000.0, // Temporary safe fallback for IDE
                onBack = { navController.popBackStack() },
                onNavigateToCurrencyConverter = { navController.navigate(Screen.CurrencyConverter) },
                onNavigateToCryptoConverter = { navController.navigate(Screen.Market) },
                onNavigateToCompoundInterest = { navController.navigate(Screen.CompoundInterest) }
            )
        }

        composable(Screen.CurrencyConverter) {
            com.example.ui.screens.CurrencyConverterScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CompoundInterest) {
            com.example.ui.screens.CompoundInterestScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SimpleInterest) {
            com.example.ui.screens.SimpleInterestScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LoanCalculator) {
            com.example.ui.screens.LoanCalculatorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GoldWage) {
            com.example.ui.screens.GoldWageScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GoldBubble) {
            com.example.ui.screens.GoldBubbleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BankAccounts) {
            com.example.ui.screens.BankAccountsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DebtCredits) {
            com.example.ui.screens.DebtCreditsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reminders) {
            com.example.ui.screens.RemindersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Goals) {
            com.example.ui.screens.GoalsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MutualFunds) {
            com.example.ui.screens.MutualFundsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OcrScanner) {
            com.example.ui.screens.OcrScannerScreen(
                viewModel = aiAnalysisViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings) {
            com.example.ui.screens.SettingsScreen(
                viewModel = settingsViewModel,
                biometricEnabled = pinManager.isBiometricEnabled(),
                onOpenPinSetup = { navController.navigate(Screen.PinSetup) },
                onOpenBiometricEnable = { navController.navigate(Screen.BiometricEnable) },
                onExportRequested = onExportRequested,
                onImportRequested = onImportRequested
            )
        }

        composable(Screen.PinSetup) {
            com.example.ui.screens.PinSetupScreen(
                onPinCreated = { pin ->
                    pinManager.setPin(pin)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.BiometricEnable) {
            com.example.ui.screens.BiometricEnableScreen(
                onEnable = {
                    pinManager.setBiometricEnabled(true)
                    navController.popBackStack()
                },
                onSkip = { navController.popBackStack() }
            )
        }
    }
}
