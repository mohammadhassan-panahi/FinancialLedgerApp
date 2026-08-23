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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CryptoAssetEntity
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

    LaunchedEffect(Unit) {
        if (allAssets.isEmpty()) {
            viewModel.refreshMarketData()
        }
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
                CryptoAssetCard(asset, onToggleWatchlist = { viewModel.toggleWatchlist(asset) })
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
            CryptoAssetCard(asset, onToggleWatchlist = { viewModel.toggleWatchlist(asset) })
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
fun CryptoAssetCard(asset: CryptoAssetEntity, onToggleWatchlist: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
