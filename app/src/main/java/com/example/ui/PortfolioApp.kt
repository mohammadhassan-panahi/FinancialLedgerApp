package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
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
import com.example.ui.LocalIsRial

@Composable
fun PortfolioApp(
    viewModel: PortfolioViewModel,
    cryptoViewModel: CryptoViewModel,
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

    val bottomNavItems = listOf(
        BottomNavItem("خانه", Screen.Dashboard, Icons.Default.Dashboard),
        BottomNavItem("بازار", Screen.Market, Icons.Default.ShowChart),
        BottomNavItem("پورتفو", Screen.Portfolio, Icons.Default.PieChart),
        BottomNavItem("اخبار", Screen.NewsHub, Icons.Default.Newspaper),
        BottomNavItem("ابزارها", Screen.Tools, Icons.Default.Build)
    )

    CompositionLocalProvider(LocalIsRial provides isRial) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                viewModel = viewModel,
                cryptoViewModel = cryptoViewModel,
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

private data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
