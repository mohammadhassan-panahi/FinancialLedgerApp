package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.HoldingSummary
import com.example.ui.components.DonutSlice
import com.example.ui.components.PortfolioDonutChart
import com.example.ui.components.SellAssetDialog
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial

private val GoldColor = Color(0xFFC9A24B)
private val UsdColor = Color(0xFF4C7A5C)
private val StockColor = Color(0xFF5B85AA)

@Composable
fun PortfolioHomeScreen(
    viewModel: PortfolioViewModel,
    onExportRequested: () -> Unit = {},
    onImportRequested: () -> Unit = {},
    isPrivacyModeEnabled: Boolean = false,
    onTogglePrivacyMode: () -> Unit = {}
) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val totalRealizedPnl by viewModel.totalRealizedPnlRial.collectAsStateWithLifecycle()
    val sellError by viewModel.sellError.collectAsStateWithLifecycle()
    val totalValue = holdings.sumOf { it.currentValueRial }
    val totalPaid = holdings.sumOf { it.totalPaidRial }
    val totalPnl = totalValue - totalPaid
    val totalPnlPercent = if (totalPaid > 0) (totalPnl / totalPaid) * 100.0 else 0.0

    val goldValue = holdings.filter { it.assetType == PortfolioAssetType.GOLD }.sumOf { it.currentValueRial }
    val usdValue = holdings.filter { it.assetType == PortfolioAssetType.USD }.sumOf { it.currentValueRial }
    val stockValue = holdings.filter { it.assetType == PortfolioAssetType.STOCK }.sumOf { it.currentValueRial }

    var menuExpanded by remember { mutableStateOf(false) }
    var sellTarget by remember { mutableStateOf<HoldingSummary?>(null) }

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
                Text("خانه", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePrivacyMode) {
                        Icon(
                            if (isPrivacyModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "مخفی‌سازی مبالغ"
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "پشتیبان‌گیری")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("خروجی گرفتن از داده‌ها") },
                                leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                                onClick = { menuExpanded = false; onExportRequested() }
                            )
                            DropdownMenuItem(
                                text = { Text("بازیابی از فایل پشتیبان") },
                                onClick = { menuExpanded = false; onImportRequested() }
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ارزش کل پرتفوی", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    com.example.ui.components.PrivacyAwareAmountText(
                        text = formatRial(totalValue),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    com.example.ui.components.PrivacyAwareAmountText(
                        text = "${formatPercentSigned(totalPnlPercent)} (${formatRial(totalPnl)})",
                        color = if (totalPnl >= 0) EmeraldProfit else RoseLoss
                    )
                }
            }
        }
        if (totalRealizedPnl != 0.0) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سود محقق‌شده (فروش‌های قبلی)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        com.example.ui.components.PrivacyAwareAmountText(
                            text = formatRial(totalRealizedPnl),
                            fontWeight = FontWeight.Bold,
                            color = if (totalRealizedPnl >= 0) EmeraldProfit else RoseLoss
                        )
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ترکیب دارایی",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    PortfolioDonutChart(
                        slices = listOf(
                            DonutSlice("طلا", goldValue, GoldColor),
                            DonutSlice("دلار", usdValue, UsdColor),
                            DonutSlice("سهام", stockValue, StockColor)
                        )
                    )
                }
            }
        }
        item {
            Text(
                "دارایی‌های من",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (holdings.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "هنوز خریدی ثبت نکردی. از تب «افزودن» شروع کن.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(holdings) { holding -> HoldingCard(holding, onSellClick = { sellTarget = holding }) }
        }
    }

    sellTarget?.let { holding ->
        SellAssetDialog(
            holding = holding,
            errorMessage = sellError,
            onDismiss = { sellTarget = null; viewModel.clearSellError() },
            onConfirm = { quantity, price ->
                viewModel.sellAsset(
                    assetType = holding.assetType,
                    assetCode = holding.assetCode,
                    assetName = holding.assetName,
                    quantitySold = quantity,
                    saleUnitPriceRial = price,
                    onSuccess = { sellTarget = null }
                )
            }
        )
    }
}

@Composable
private fun HoldingCard(holding: HoldingSummary, onSellClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(holding.assetName, fontWeight = FontWeight.Bold)
                TextButton(onClick = onSellClick) { Text("فروش") }
            }
            com.example.ui.components.PrivacyAwareAmountText(
                text = formatRial(holding.currentValueRial),
                style = MaterialTheme.typography.titleMedium
            )
            com.example.ui.components.PrivacyAwareAmountText(
                text = "${formatPercentSigned(holding.profitLossPercent)} (${formatRial(holding.profitLossRial)})",
                color = if (holding.profitLossRial >= 0) EmeraldProfit else RoseLoss
            )
        }
    }
}
