package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.MarketScannerScreen
import com.example.ui.dashboard.MarketScannerViewModel
import com.example.ui.social.SocialHubScreen
import com.example.ui.tools.ToolsScreen

object Screen {
    const val Dashboard = "dashboard"
    const val Portfolio = "portfolio"
    const val AiMentor = "ai_mentor"
    const val SocialHub = "social_hub"
    const val Tools = "tools"
    const val MarketScanner = "market_scanner"
    const val MarketPulse = "market_pulse"
    const val RiskAssessment = "risk_assessment"
    const val InvestmentRoadmap = "investment_roadmap"
    const val InflationCalculator = "inflation_calculator"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: com.example.ui.viewmodel.PortfolioViewModel,
    aiAnalysisViewModel: com.example.ui.viewmodel.AiAnalysisViewModel,
    socialHubViewModel: com.example.ui.viewmodel.SocialHubViewModel,
    riskAssessmentViewModel: com.example.ui.viewmodel.RiskAssessmentViewModel,
    marketScannerViewModel: MarketScannerViewModel,
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
        composable(Screen.Portfolio) {
            com.example.ui.screens.PortfolioHomeScreen(
                viewModel = viewModel,
                onOpenAiAnalysis = { navController.navigate(Screen.AiMentor) }
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
            ToolsScreen()
        }
        composable(Screen.MarketScanner) {
            MarketScannerScreen(viewModel = marketScannerViewModel)
        }
        composable(Screen.MarketPulse) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("نبض بازار (Market Pulse) - در حال توسعه")
            }
        }
        composable(Screen.RiskAssessment) {
            com.example.ui.screens.RiskAssessmentScreen(
                viewModel = riskAssessmentViewModel,
                onFinished = { navController.popBackStack() }
            )
        }
        composable(Screen.InvestmentRoadmap) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("نقشه راه سرمایه‌گذاری - در حال توسعه")
            }
        }
        composable(Screen.InflationCalculator) {
            com.example.ui.screens.InflationCalculatorScreen(viewModel = viewModel)
        }
    }
}
