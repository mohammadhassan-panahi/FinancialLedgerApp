package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CryptoViewModel
import com.example.ui.viewmodel.PortfolioViewModel

@Composable
fun MarketHubScreen(
    portfolioViewModel: PortfolioViewModel,
    cryptoViewModel: CryptoViewModel,
    onNavigateToIntelligence: () -> Unit = {},
    onNavigateToSection: (String) -> Unit = {}
) {
    // We now use MarketDashboardScreen as the primary entry point
    MarketDashboardScreen(
        viewModel = portfolioViewModel,
        onNavigateToSection = onNavigateToSection
    )
}
