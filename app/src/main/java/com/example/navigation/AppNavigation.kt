package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalculatorsHubScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.MarketPortfolioScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.SwapMarketScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.MarketPortfolioViewModel

object Route {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Analytics = "analytics"
    const val Deposit = "deposit"
    const val Transfer = "transfer"
    const val Swap = "swap"
    const val Calculators = "calculators"
    const val Settings = "settings"
}

@Composable
fun AppNavigation(
    viewModel: MarketPortfolioViewModel,
    calculatorViewModel: CalculatorViewModel,
    startDestination: String = Route.Home,
    onFinishOnboarding: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.Onboarding) {
            OnboardingScreen(
                onFinishOnboarding = {
                    onFinishOnboarding?.invoke()
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Home) {
            MarketPortfolioScreen(
                viewModel = viewModel,
                onNavigateToSecurity = { navController.navigate(Route.Settings) },
                onNavigateToAnalytics = { navController.navigate(Route.Analytics) },
                onNavigateToDeposit = { navController.navigate(Route.Deposit) },
                onNavigateToTransfer = { navController.navigate(Route.Transfer) },
                onNavigateToSwap = { navController.navigate(Route.Swap) },
                onNavigateToCalculators = { navController.navigate(Route.Calculators) }
            )
        }
        composable(Route.Calculators) {
            CalculatorsHubScreen(
                viewModel = calculatorViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Analytics) {
            AnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Deposit) {
            DepositScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Transfer) {
            TransferScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Swap) {
            SwapMarketScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Settings) {
            SecurityScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
