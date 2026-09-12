package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.NavGraph
import com.example.navigation.Screen
import com.example.data.repository.UserPreferencesRepository
import com.example.security.BiometricAuthManager
import com.example.security.PinManager
import com.example.ui.viewmodel.CryptoViewModel
import com.example.ui.dashboard.MarketScannerViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.components.DaraBottomBar

@Composable
fun PortfolioApp(
    viewModel: PortfolioViewModel,
    cryptoViewModel: CryptoViewModel,
    marketPortfolioViewModel: com.example.ui.viewmodel.MarketPortfolioViewModel,
    marketScannerViewModel: MarketScannerViewModel,
    calculatorViewModel: com.example.ui.viewmodel.CalculatorViewModel,
    aiAnalysisViewModel: com.example.ui.viewmodel.AiAnalysisViewModel,
    riskAssessmentViewModel: com.example.ui.viewmodel.RiskAssessmentViewModel,
    settingsViewModel: SettingsViewModel,
    newsViewModel: com.example.ui.viewmodel.NewsViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    biometricAuthManager: BiometricAuthManager,
    pinManager: PinManager,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit
) {
    val navController = rememberNavController()
    val currencyUnit by settingsViewModel.currencyUnit.collectAsState()
    val isRial = currencyUnit == "RIAL"

    val isOnboardingCompleted by userPreferencesRepository.isOnboardingCompleted.collectAsState(initial = null)
    val pinSet = pinManager.isPinSet()
    var isUnlocked by remember { mutableStateOf(!pinSet) }

    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted == false) {
            navController.navigate(Screen.Onboarding) {
                popUpTo(0)
            }
        } else if (pinSet && !isUnlocked) {
            navController.navigate(Screen.PinEntry) {
                popUpTo(0)
            }
        }
    }

    CompositionLocalProvider(LocalIsRial provides isRial) {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                DaraBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            // Use a Box to ensure the bottom bar doesn't overlap content poorly if needed,
            // though Scaffold handles padding.
            NavGraph(
                navController = navController,
                viewModel = viewModel,
                cryptoViewModel = cryptoViewModel,
                marketPortfolioViewModel = marketPortfolioViewModel,
                calculatorViewModel = calculatorViewModel,
                aiAnalysisViewModel = aiAnalysisViewModel,
                riskAssessmentViewModel = riskAssessmentViewModel,
                settingsViewModel = settingsViewModel,
                newsViewModel = newsViewModel,
                marketScannerViewModel = marketScannerViewModel,
                userPreferencesRepository = userPreferencesRepository,
                biometricAuthManager = biometricAuthManager,
                pinManager = pinManager,
                onExportRequested = onExportRequested,
                onImportRequested = onImportRequested,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
