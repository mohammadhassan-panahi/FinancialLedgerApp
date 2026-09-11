package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CryptoAssetEntity
import com.example.data.local.GlobalMetricsEntity
import com.example.ui.components.CryptoIcon
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CryptoViewModel
import com.example.util.PersianNumberUtils
import com.example.util.formatUsd
import java.math.BigDecimal
import java.util.Locale

@Composable
fun CryptoMarketDashboardScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit,
    onAssetClick: (CryptoAssetEntity) -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToWatchlist: () -> Unit
) {
    val allAssets by viewModel.allAssets.collectAsStateWithLifecycle()
    val globalMetrics by viewModel.globalMetrics.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("همه", "برترین‌ها", "سودده‌ترین", "واچ‌لیست")

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            DashboardHeader(
                onBack = onBack, 
                onRefresh = { viewModel.refreshMarketData() }, 
                isRefreshing = isRefreshing,
                onNavigateToWatchlist = onNavigateToWatchlist
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Global Market Metrics
            item {
                GlobalMetricsCard(metrics = globalMetrics)
            }

            // Section 2: Quick Actions (Scanner & Market Sentiment)
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MarketScannerAction(modifier = Modifier.weight(1f), onClick = onNavigateToScanner)
                    MarketSentimentCard(modifier = Modifier.weight(1f), metrics = globalMetrics)
                }
            }

            // Section 3: Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = IndigoElectric,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = IndigoElectric
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, style = DaraTypography.labelMedium, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            // Section 4: Crypto List
            val filteredAssets = when (selectedTab) {
                0 -> allAssets
                1 -> allAssets.sortedByDescending { it.marketCapUsd }.take(20)
                2 -> allAssets.sortedByDescending { it.percentChange24h ?: BigDecimal.ZERO }.take(20)
                3 -> allAssets.filter { it.isInWatchlist }
                else -> allAssets
            }

            if (filteredAssets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("ارزی در این دسته‌بندی یافت نشد", color = Slate600, style = DaraTypography.bodySmall)
                    }
                }
            } else {
                items(filteredAssets) { asset ->
                    CryptoMarketItem(asset = asset, onClick = { onAssetClick(asset) })
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DashboardHeader(onBack: () -> Unit, onRefresh: () -> Unit, isRefreshing: Boolean, onNavigateToWatchlist: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate50)
                }
                Text("بازار کریپتوکارنسی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = onNavigateToWatchlist) {
                    Icon(Icons.Default.Star, null, tint = RefinedAmberGold)
                }
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    Icon(Icons.Default.Refresh, null, tint = if (isRefreshing) Slate600 else IndigoElectric)
                }
            }
        }
    }
}

@Composable
fun GlobalMetricsCard(metrics: GlobalMetricsEntity?) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ارزش کل بازار کریپتو", style = DaraTypography.labelSmall, color = Slate400)
                    Text(
                        text = metrics?.totalMarketCapUsd?.let { formatUsd(it) } ?: "$---",
                        style = DaraTypography.titleLarge,
                        color = Slate50,
                        fontWeight = FontWeight.Black
                    )
                }
                Surface(color = EmeraldCore.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = EmeraldCore, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live", style = DaraTypography.labelSmall, color = EmeraldCore, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                MetricSmallItem("BTC Dominance", "${metrics?.btcDominance ?: "--"}%", IndigoElectric)
                MetricSmallItem("ETH Dominance", "${metrics?.ethDominance ?: "--"}%", Color(0xFF8B5CF6))
                MetricSmallItem("Coins", "${metrics?.activeCryptocurrencies ?: "--"}", EmeraldCore)
            }
        }
    }
}

@Composable
fun MetricSmallItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = DaraTypography.labelSmall, color = Slate600)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Text(value, style = DaraTypography.labelSmall, color = Slate50, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MarketScannerAction(modifier: Modifier, onClick: () -> Unit) {
    DaraGlassCard(modifier = modifier.clickable { onClick() }) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = IndigoElectric.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric, modifier = Modifier.size(16.dp))
                }
            }
            Column {
                Text("اسکنر فرصت‌ها", style = DaraTypography.labelSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text("رتبه‌بندی هوشمند", style = DaraTypography.labelSmall, color = Slate600)
            }
        }
    }
}

@Composable
fun MarketSentimentCard(modifier: Modifier, metrics: GlobalMetricsEntity?) {
    DaraGlassCard(modifier = modifier) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = RefinedAmberGold.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Speed, null, tint = RefinedAmberGold, modifier = Modifier.size(16.dp))
                }
            }
            Column {
                Text("ترس و طمع", style = DaraTypography.labelSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text(metrics?.fearAndGreedLabel ?: "نامشخص", style = DaraTypography.labelSmall, color = RefinedAmberGold)
            }
        }
    }
}

@Composable
fun CryptoMarketItem(asset: CryptoAssetEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CryptoIcon(cmcId = asset.cmcId, symbol = asset.symbol, size = 40.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text(
                    text = "#${asset.cmcRank ?: "--"} | ${asset.symbol}",
                    style = DaraTypography.labelSmall,
                    color = Slate600
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatUsd(asset.priceUsd ?: BigDecimal.ZERO),
                    style = DaraTypography.titleSmall,
                    color = Slate50,
                    fontWeight = FontWeight.Bold
                )
                val change = asset.percentChange24h ?: BigDecimal.ZERO
                Text(
                    text = "${if (change >= BigDecimal.ZERO) "+" else ""}${String.format(Locale.US, "%.2f", change.toDouble())}%",
                    style = DaraTypography.labelSmall,
                    color = if (change >= BigDecimal.ZERO) EmeraldCore else RoseCoral,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
