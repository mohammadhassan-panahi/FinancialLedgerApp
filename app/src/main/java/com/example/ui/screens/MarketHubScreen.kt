package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.CryptoViewModel
import com.example.ui.viewmodel.PortfolioViewModel

/**
 * Hub screen for the "بازار" (Market) bottom-nav tab.
 * Replaces the old "در حال توسعه" placeholder with real, working sub-screens:
 * طلا/دلار, بورس, رمزارز, مبدل ارز — each already fully implemented, just
 * never reachable from navigation before.
 */
@Composable
fun MarketHubScreen(
    portfolioViewModel: PortfolioViewModel,
    cryptoViewModel: CryptoViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("طلا/دلار", "بورس", "رمزارز", "مبدل ارز")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                )
            }
        }

        when (selectedTab) {
            0 -> GoldDollarScreen(viewModel = portfolioViewModel)
            1 -> StockMarketScreen(viewModel = portfolioViewModel)
            2 -> CryptoScreen(viewModel = cryptoViewModel)
            3 -> CurrencyConverterScreen()
        }
    }
}
