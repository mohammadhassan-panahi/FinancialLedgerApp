package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    
    // Breadth analysis (for simulated heatmap feel)
    val advancedStats = remember(watchlist) {
        if (watchlist.isEmpty()) null else {
            val gainers = watchlist.count { it.changePercent > 0 }
            val losers = watchlist.count { it.changePercent < 0 }
            val neutral = watchlist.size - gainers - losers
            val avgChange = watchlist.map { it.changePercent }.average()
            MarketBreadth(gainers, losers, neutral, avgChange)
        }
    }

    var newSymbol by remember { mutableStateOf("") }
    var alertTarget by remember { mutableStateOf<StockSymbolEntity?>(null) }
    var removeTarget by remember { mutableStateOf<StockSymbolEntity?>(null) }

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

        advancedStats?.let { stats ->
            item {
                MarketBreadthCard(stats)
            }
        }

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
            items(watchlist) { symbol ->
                StockCard(symbol, onSetAlert = { alertTarget = symbol }, onRemove = { removeTarget = symbol })
            }
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

    removeTarget?.let { stock ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("حذف از واچ‌لیست") },
            text = { Text("نماد «${stock.symbol}» از واچ‌لیست حذف بشه؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeSymbolFromWatchlist(stock.symbol)
                    removeTarget = null
                }) { Text("حذف", color = RoseLoss) }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) { Text("انصراف") }
            }
        )
    }
}

data class MarketBreadth(val gainers: Int, val losers: Int, val neutral: Int, val avgChange: Double)

@Composable
fun MarketBreadthCard(stats: MarketBreadth) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("تحلیل وضعیت واچ‌لیست", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("مثبت", color = EmeraldProfit, style = MaterialTheme.typography.labelSmall)
                    Text("${stats.gainers}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("منفی", color = RoseLoss, style = MaterialTheme.typography.labelSmall)
                    Text("${stats.losers}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("ثابت", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text("${stats.neutral}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("میانگین تغییر", style = MaterialTheme.typography.labelSmall)
                    Text(
                        com.example.util.formatPercentSigned(stats.avgChange),
                        color = if (stats.avgChange >= 0) EmeraldProfit else RoseLoss,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Simple bar chart for gainers/losers
            val total = stats.gainers + stats.losers + stats.neutral
            if (total > 0) {
                Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
                    Box(modifier = Modifier.weight(stats.gainers.toFloat().coerceAtLeast(0.1f)).fillMaxSize().background(EmeraldProfit))
                    Box(modifier = Modifier.weight(stats.neutral.toFloat().coerceAtLeast(0.1f)).fillMaxSize().background(Color.Gray))
                    Box(modifier = Modifier.weight(stats.losers.toFloat().coerceAtLeast(0.1f)).fillMaxSize().background(RoseLoss))
                }
            }
        }
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
private fun StockCard(symbol: StockSymbolEntity, onSetAlert: () -> Unit, onRemove: () -> Unit) {
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
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف از واچ‌لیست", tint = RoseLoss)
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
