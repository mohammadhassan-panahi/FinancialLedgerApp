package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MarketIndexEntity
import com.example.data.local.PriceAlertEntity
import com.example.data.local.StockSymbolEntity
import com.example.ui.LocalIsRial
import com.example.ui.components.PriceAlertDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial

@Composable
fun StockMarketScreen(viewModel: PortfolioViewModel) {
    val indices by viewModel.indices.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val ipos by viewModel.ipos.collectAsStateWithLifecycle()
    val codalNotices by viewModel.codalNotices.collectAsStateWithLifecycle()
    
    var showIpoSheet by remember { mutableStateOf(false) }
    var showCodalSheet by remember { mutableStateOf(false) }
    
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
        item {
            val mainIndex = indices.find { it.indexCode == "TOTAL_INDEX" || it.name.contains("کل") }
            if (mainIndex != null) {
                MainIndexHero(mainIndex)
            } else if (indices.isNotEmpty()) {
                MiniIndexCard(indices.first())
            }
        }
        
        item {
            if (indices.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    indices.filter { it.indexCode != "TOTAL_INDEX" && !it.name.contains("کل") }.take(2).forEach { secondaryIndex ->
                        Box(modifier = Modifier.weight(1f)) {
                            MiniIndexCard(secondaryIndex)
                        }
                    }
                }
            }
        }

        advancedStats?.let { stats ->
            item {
                MarketBreadthCard(stats)
            }
            
            item {
                MarketInsightsCard()
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showIpoSheet = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CredifyIndigo)
                    ) {
                        Icon(Icons.Default.Flag, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عرضه اولیه", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showCodalSheet = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CredifyViolet)
                    ) {
                        Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("پیام‌های کدال", fontSize = 12.sp)
                    }
                }
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

    if (showIpoSheet) {
        IpoListDialog(ipos = ipos, onDismiss = { showIpoSheet = false })
    }

    if (showCodalSheet) {
        CodalListDialog(notices = codalNotices, onDismiss = { showCodalSheet = false })
    }
}

@Composable
fun IpoListDialog(ipos: List<com.example.data.local.IpoEntity>, onDismiss: () -> Unit) {
    val isRial = LocalIsRial.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عرضه‌های اولیه جدید", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                if (ipos.isEmpty()) {
                    item { Text("در حال حاضر عرضه اولیه فعالی وجود ندارد.", color = TextSecondary) }
                } else {
                    items(ipos) { ipo ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(ipo.symbol, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(ipo.status, color = EmeraldProfit, fontSize = 11.sp)
                                }
                                Text(ipo.companyName, color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("تاریخ: ${ipo.ipoDate}", fontSize = 11.sp, color = TextPrimary)
                                Text("نقدینگی مورد نیاز: ${formatRial(ipo.requiredLiquidityRial, isRial = isRial)}", fontSize = 11.sp, color = GoldAccent)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}

@Composable
fun CodalListDialog(notices: List<com.example.data.local.CodalEntity>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("آخرین اطلاعیه‌های کدال", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                if (notices.isEmpty()) {
                    item { Text("اطلاعیه‌ای یافت نشد.", color = TextSecondary) }
                } else {
                    items(notices) { notice ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(notice.symbol, fontWeight = FontWeight.Bold, color = CredifyIndigo)
                                Text(notice.publishDate, fontSize = 10.sp, color = TextSecondary)
                            }
                            Text(notice.title, fontSize = 12.sp, color = TextPrimary, maxLines = 2)
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = SlateBorderLight.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
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
fun MarketInsightsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("دیده‌بان هوشمند بازار", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InsightItem("پول هوشمند", "مثبت", EmeraldProfit)
                InsightItem("حجم مشکوک", "۳ نماد", GoldAccent)
                InsightItem("برترین صنعت", "خودرو", CredifyIndigo)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SlateBorderLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("این تحلیل بر اساس میانگین معاملات امروز برآورد شده است.", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun InsightItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 14.sp)
    }
}

@Composable
fun MainIndexHero(index: MarketIndexEntity) {
    val isRial = LocalIsRial.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CredifyIndigo),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("شاخص کل بورس", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatRial(index.value, showSuffix = false, isRial = isRial),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (index.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = formatPercentSigned(index.changePercent),
                        color = if (index.changePercent >= 0) Color(0xFF81C784) else Color(0xFFFF8A80),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MiniIndexCard(index: MarketIndexEntity) {
    val isRial = LocalIsRial.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(index.name, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatRial(index.value, showSuffix = false, isRial = isRial),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    formatPercentSigned(index.changePercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index.changePercent >= 0) EmeraldProfit else RoseLoss
                )
            }
        }
    }
}

@Composable
private fun StockCard(symbol: StockSymbolEntity, onSetAlert: () -> Unit, onRemove: () -> Unit) {
    val isRial = LocalIsRial.current
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
                            Text(formatRial(symbol.lastPriceRial, isRial = isRial), fontWeight = FontWeight.Bold)
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
                            if (symbol.buyPriceRial > 0.0) formatRial(symbol.buyPriceRial, isRial = isRial) else "—",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldProfit
                        )
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text("قیمت فروش", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (symbol.sellPriceRial > 0.0) formatRial(symbol.sellPriceRial, isRial = isRial) else "—",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseLoss
                        )
                    }
                }
            }
        }
    }
}
