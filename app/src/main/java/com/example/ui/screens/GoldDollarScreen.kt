package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AlertDirection
import com.example.data.local.MarketRateEntity
import com.example.data.local.PriceAlertEntity
import com.example.ui.components.PriceAlertDialog
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import com.example.util.priceRial

@Composable
fun GoldDollarScreen(viewModel: PortfolioViewModel) {
    val allRates by viewModel.marketRates.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var alertTarget by remember { mutableStateOf<MarketRateEntity?>(null) }

    // Prefer live rates over the leftover offline/demo seed rows (different assetCode, e.g.
    // seed "GOLD_18K" vs live "IR_GOLD_18K") so the same asset doesn't show twice with two
    // different, conflicting prices.
    val hasLiveRates = allRates.any { !it.isOfflineRate }
    val rates = if (hasLiveRates) allRates.filter { !it.isOfflineRate } else allRates

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("طلا و دلار", style = MaterialTheme.typography.titleMedium)
                androidx.compose.material3.Button(
                    onClick = { viewModel.refreshAll() },
                    enabled = !isRefreshing
                ) { Text(if (isRefreshing) "در حال بروزرسانی..." else "بروزرسانی") }
            }
        }
        if (isOffline) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "اتصال برقرار نیست — نرخ‌های آخرین بروزرسانی نمایش داده می‌شوند.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (rates.isEmpty()) {
            item {
                Text("در حال دریافت نرخ‌ها...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(rates) { rate -> RateCard(rate, onSetAlert = { alertTarget = rate }) }
        }
    }

    alertTarget?.let { rate ->
        PriceAlertDialog(
            assetName = rate.name,
            currentPriceRial = rate.priceRial,
            onDismiss = { alertTarget = null },
            onConfirm = { target, direction ->
                viewModel.addAlert(
                    PriceAlertEntity(
                        assetCode = rate.assetCode,
                        assetName = rate.name,
                        targetPriceRial = target,
                        direction = direction
                    )
                )
                alertTarget = null
            }
        )
    }
}

@Composable
private fun RateCard(rate: MarketRateEntity, onSetAlert: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(rate.name, fontWeight = FontWeight.Bold)
                Text(
                    formatPercentSigned(rate.changePercent),
                    color = if (rate.changePercent >= 0) EmeraldProfit else RoseLoss
                )
            }
            Row {
                Text(
                    formatRial(rate.priceRial),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                IconButton(onClick = onSetAlert) {
                    Icon(Icons.Default.Notifications, contentDescription = "تنظیم هشدار قیمت")
                }
            }
        }
    }
}
