package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
    onNavigateToIntelligence: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("طلا/دلار", "بورس", "رمزارز", "مبدل")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSlate900)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("بازارها", style = DaraTypography.headlineSmall, color = Slate50, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNavigateToIntelligence) {
                Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, null, tint = IndigoElectric)
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ObsidianSlate900,
            contentColor = IndigoElectric,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = IndigoElectric,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            title, 
                            style = DaraTypography.labelLarge,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Slate50 else Slate400
                        ) 
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> GoldDollarScreen(viewModel = portfolioViewModel)
                1 -> StockMarketScreen(viewModel = portfolioViewModel)
                2 -> CryptoScreen(viewModel = cryptoViewModel)
                3 -> CurrencyConverterScreen()
            }
        }
    }
}
