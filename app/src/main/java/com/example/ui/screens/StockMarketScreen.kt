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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MarketIndexEntity
import com.example.data.local.PriceAlertEntity
import com.example.data.local.StockSymbolEntity
import com.example.ui.components.PriceAlertDialog
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial

@Composable
fun StockMarketScreen(viewModel: PortfolioViewModel) {
    val indices by viewModel.indices.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var newSymbol by remember { mutableStateOf("") }
    var alertTarget by remember { mutableStateOf<StockSymbolEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("بورس ایران", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { viewModel.refreshAll(watchlistSymbols = watchlist.map { it.symbol }) },
                    enabled = !isRefreshing
                ) { Text(if (isRefreshing) "در حال بروزرسانی..." else "بروزرسانی") }
            }
        }
        items(indices) { index -> IndexCard(index) }
        item {
            Text(
                "واچ‌لیست من",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newSymbol,
                    onValueChange = { newSymbol = it },
                    label = { Text("نماد بورسی (مثلاً فولاد)") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (newSymbol.isNotBlank()) {
                        viewModel.addSymbolToWatchlist(newSymbol.trim(), newSymbol.trim())
                        viewModel.refreshAll(watchlistSymbols = watchlist.map { it.symbol } + newSymbol.trim())
                        newSymbol = ""
                    }
                }) { Text("افزودن") }
            }
        }
        if (watchlist.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "نمادی به واچ‌لیست اضافه نکردی.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(watchlist) { symbol -> StockCard(symbol, onSetAlert = { alertTarget = symbol }) }
        }
    }

    alertTarget?.let { stock ->
        PriceAlertDialog(
            assetName = stock.fullName,
            currentPriceRial = stock.lastPriceRial,
            onDismiss = { alertTarget = null },
            onConfirm = { target, direction ->
                viewModel.addAlert(
                    PriceAlertEntity(
                        assetCode = stock.symbol,
                        assetName = stock.fullName,
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
private fun IndexCard(index: MarketIndexEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(index.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatRial(index.value, showSuffix = false),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatPercentSigned(index.changePercent),
                    color = if (index.changePercent >= 0) EmeraldProfit else RoseLoss
                )
            }
        }
    }
}

@Composable
private fun StockCard(symbol: StockSymbolEntity, onSetAlert: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(symbol.symbol, fontWeight = FontWeight.Bold)
                    Text(symbol.fullName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (symbol.lastPriceRial > 0.0) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(formatRial(symbol.lastPriceRial), fontWeight = FontWeight.Bold)
                            Text(
                                formatPercentSigned(symbol.changePercent),
                                color = if (symbol.changePercent >= 0) EmeraldProfit else RoseLoss
                            )
                        }
                    } else {
                        Text(
                            "در انتظار قیمت — بروزرسانی کن",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onSetAlert) {
                        Icon(Icons.Default.Notifications, contentDescription = "تنظیم هشدار قیمت")
                    }
                }
            }
            if (symbol.buyPriceRial > 0.0 || symbol.sellPriceRial > 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("قیمت خرید", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (symbol.buyPriceRial > 0.0) formatRial(symbol.buyPriceRial) else "—",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldProfit
                        )
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text("قیمت فروش", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (symbol.sellPriceRial > 0.0) formatRial(symbol.sellPriceRial) else "—",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseLoss
                        )
                    }
                }
            }
        }
    }
}
