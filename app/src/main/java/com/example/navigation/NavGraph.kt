package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.data.repository.UserPreferencesRepository
import com.example.security.BiometricAuthManager
import com.example.security.PinManager
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.MarketScannerScreen
import com.example.ui.dashboard.MarketScannerViewModel
import com.example.ui.social.SocialHubScreen
import com.example.ui.tools.ToolsScreen
import com.example.ui.viewmodel.CryptoViewModel

/** Central registry of every route in the app. */
object Screen {
    const val Dashboard = "dashboard"
    const val Market = "market"
    const val Portfolio = "portfolio"
    const val AiMentor = "ai_mentor"
    const val SocialHub = "social_hub"
    const val Tools = "tools"
    const val MarketScanner = "market_scanner"
    const val RiskAssessment = "risk_assessment"
    const val InvestmentRoadmap = "investment_roadmap"
    const val InflationCalculator = "inflation_calculator"

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
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: com.example.ui.viewmodel.PortfolioViewModel,
    cryptoViewModel: CryptoViewModel,
    calculatorViewModel: com.example.ui.viewmodel.CalculatorViewModel,
    aiAnalysisViewModel: com.example.ui.viewmodel.AiAnalysisViewModel,
    socialHubViewModel: com.example.ui.viewmodel.SocialHubViewModel,
    riskAssessmentViewModel: com.example.ui.viewmodel.RiskAssessmentViewModel,
    marketScannerViewModel: MarketScannerViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    biometricAuthManager: BiometricAuthManager,
    pinManager: PinManager,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard,
        modifier = modifier
    ) {
        composable(Screen.Dashboard) {
            DashboardScreen(onNavigateToScanner = { navController.navigate(Screen.MarketScanner) })
        }

        composable(Screen.Market) {
            com.example.ui.screens.MarketHubScreen(
                portfolioViewModel = viewModel,
                cryptoViewModel = cryptoViewModel
            )
        }

        composable(Screen.Portfolio) {
            com.example.ui.screens.PortfolioHomeScreen(
                viewModel = viewModel,
                onExportRequested = onExportRequested,
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

        composable(Screen.SocialHub) {
            SocialHubScreen(viewModel = socialHubViewModel)
        }

        composable(Screen.Tools) {
            ToolsScreen(
                onOpenCalculators = { navController.navigate(Screen.CalculatorsHub) },
                onOpenRiskAssessment = { navController.navigate(Screen.RiskAssessment) },
                onOpenOcrScanner = { navController.navigate(Screen.OcrScanner) },
                onOpenInvestmentRoadmap = { navController.navigate(Screen.InvestmentRoadmap) }
            )
        }

        composable(Screen.MarketScanner) {
            MarketScannerScreen(viewModel = marketScannerViewModel)
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
            com.example.ui.screens.AddPurchaseScreen(viewModel = viewModel)
        }

        composable(Screen.CalculatorsHub) {
            val holdings by viewModel.holdings.collectAsStateWithLifecycle()
            val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
            val cryptoAssets by viewModel.cryptoAssets.collectAsStateWithLifecycle()
            com.example.ui.screens.CalculatorsHubScreen(
                viewModel = calculatorViewModel,
                onBack = { navController.popBackStack() },
                holdings = holdings,
                marketRates = marketRates,
                cryptoAssets = cryptoAssets
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
