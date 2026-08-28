package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crypto.ScoringEngine
import com.example.data.local.CryptoAssetEntity
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.CryptoViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatUsd

@Composable
fun CryptoScreen(viewModel: CryptoViewModel) {
    val allAssets by viewModel.allAssets.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val globalSnapshot by viewModel.globalSnapshot.collectAsStateWithLifecycle()
    val selectedAsset by viewModel.selectedAsset.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (allAssets.isEmpty()) {
            viewModel.refreshMarketData()
        }
    }

    if (selectedAsset != null) {
        CryptoDetailScreen(viewModel = viewModel, asset = selectedAsset!!, onBack = { viewModel.closeDetail() })
        return
    }

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
                Text("بازار کریپتوکارنسی", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { viewModel.refreshMarketData() },
                    enabled = !isRefreshing
                ) { Text(if (isRefreshing) "در حال بروزرسانی..." else "بروزرسانی") }
            }
        }

        globalSnapshot?.let { snapshot ->
            item {
                GlobalMetricsCard(snapshot)
            }
        }

        if (watchlist.isNotEmpty()) {
            item {
                Text(
                    "واچ‌لیست من",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(watchlist) { asset ->
                CryptoAssetCard(
                    asset,
                    onToggleWatchlist = { viewModel.toggleWatchlist(asset) },
                    onClick = { viewModel.selectAsset(asset) }
                )
            }
        }

        item {
            Text(
                "برترین‌های بازار",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allAssets) { asset ->
            CryptoAssetCard(
                asset,
                onToggleWatchlist = { viewModel.toggleWatchlist(asset) },
                onClick = { viewModel.selectAsset(asset) }
            )
        }
    }
}

@Composable
fun GlobalMetricsCard(snapshot: com.example.data.repository.GlobalMarketSnapshot) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("وضعیت کلی بازار (USD)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ارزش کل بازار", style = MaterialTheme.typography.labelSmall)
                    Text(formatUsd(snapshot.totalMarketCapUsd ?: 0.0), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("سلطه بیت‌کوین", style = MaterialTheme.typography.labelSmall)
                    Text("${snapshot.btcDominance?.let { "%.1f".format(it) } ?: "—"}%", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CryptoAssetCard(asset: CryptoAssetEntity, onToggleWatchlist: () -> Unit, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CryptoIcon(cmcId = asset.cmcId, symbol = asset.symbol, size = 32.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, fontWeight = FontWeight.Bold)
                Text(asset.symbol, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatUsd(asset.priceUsd ?: 0.0), fontWeight = FontWeight.Bold)
                Text(
                    formatPercentSigned(asset.percentChange24h ?: 0.0),
                    color = if ((asset.percentChange24h ?: 0.0) >= 0) EmeraldProfit else RoseLoss,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = onToggleWatchlist) {
                Icon(
                    imageVector = if (asset.isInWatchlist) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "واچ‌لیست",
                    tint = if (asset.isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Phase 2 (per-coin info) + Phase 5 (scoring, limited to what's actually computable — see
 * ScoringEngine's doc comment), for one crypto asset.
 */
@Composable
fun CryptoDetailScreen(viewModel: CryptoViewModel, asset: CryptoAssetEntity, onBack: () -> Unit) {
    val info by viewModel.selectedInfo.collectAsStateWithLifecycle()
    val isLoadingInfo by viewModel.isLoadingInfo.collectAsStateWithLifecycle()

    val fundamental = remember(asset) { ScoringEngine.fundamentalScore(asset) }
    val risk = remember(asset) { ScoringEngine.riskScore(asset) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                }
                CryptoIcon(cmcId = asset.cmcId, symbol = asset.symbol, size = 48.dp)
                Column {
                    Text(asset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(asset.symbol, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(formatUsd(asset.priceUsd ?: 0.0), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "تغییر ۲۴ ساعته: ${formatPercentSigned(asset.percentChange24h ?: 0.0)}",
                        color = if ((asset.percentChange24h ?: 0.0) >= 0) EmeraldProfit else RoseLoss
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رتبه بازار", style = MaterialTheme.typography.labelSmall)
                        Text("#${asset.cmcRank ?: "—"}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ارزش بازار", style = MaterialTheme.typography.labelSmall)
                        Text(formatUsd(asset.marketCapUsd ?: 0.0), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("حجم معاملات ۲۴ ساعته", style = MaterialTheme.typography.labelSmall)
                        Text(formatUsd(asset.volume24hUsd ?: 0.0), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("عرضه در گردش", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${asset.circulatingSupply?.let { "%,.0f".format(it) } ?: "—"} ${asset.symbol}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { ScoreCard(title = "امتیاز بنیادی (Fundamental)", result = fundamental, barColor = MaterialTheme.colorScheme.primary) }
        item { ScoreCard(title = "امتیاز ریسک (0=کم، 100=زیاد)", result = risk, barColor = RoseLoss) }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اطلاعات پایه", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    when {
                        isLoadingInfo -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        info == null -> Text("اطلاعات موجود نیست", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else -> {
                            InfoRow("دسته‌بندی", info?.category ?: "اطلاعات موجود نیست")
                            InfoRow("وب‌سایت", info?.websiteUrl ?: "اطلاعات موجود نیست")
                            InfoRow("وایت‌پیپر", info?.whitepaperUrl ?: "اطلاعات موجود نیست")
                            InfoRow("اکسپلورر", info?.explorerUrl ?: "اطلاعات موجود نیست")
                            if (!info?.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(info?.description ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "امتیازهای بالا فقط بر پایه‌ی داده‌ی بازار (رتبه، ارزش بازار، نوسان، عرضه) محاسبه شده‌اند و توصیه‌ی خرید/فروش نیستند. امتیازهای امنیت، توسعه‌دهندگان، و تحلیل تکنیکال به منابع داده‌ی دیگری نیاز دارند که هنوز به اپ اضافه نشده‌اند.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ScoreCard(title: String, result: ScoringEngine.ScoreResult, barColor: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("${result.score}/100", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { result.score / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = barColor
            )
            Text(result.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
