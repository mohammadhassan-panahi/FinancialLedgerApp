package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CryptoAssetEntity
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.*
import com.example.ui.viewmodel.CryptoViewModel
import com.example.util.PersianNumberUtils
import com.example.util.formatPercentSigned
import com.example.util.formatUsd
import java.math.BigDecimal

@Composable
fun CryptoScreen(viewModel: CryptoViewModel, usdRateToman: BigDecimal = BigDecimal.valueOf(65000.0)) {
    val allAssets by viewModel.allAssets.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedAsset by viewModel.selectedAsset.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (allAssets.isEmpty()) {
            viewModel.refreshMarketData()
        }
    }

    if (selectedAsset != null) {
        CryptoDetailScreen(
            viewModel = viewModel,
            asset = selectedAsset!!, 
            usdRateToman = usdRateToman,
            onBack = { viewModel.closeDetail() }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("بازار کریپتوکارنسی", style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.refreshMarketData() }, enabled = !isRefreshing) {
                    if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = IndigoElectric)
                    else Icon(Icons.Default.Refresh, null, tint = IndigoElectric)
                }
            }
        }

        if (watchlist.isNotEmpty()) {
            item {
                Text("واچ‌لیست من", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
            }
            items(watchlist) { asset ->
                CryptoAssetCardPremium(
                    asset,
                    usdRateToman = usdRateToman,
                    onToggleWatchlist = { viewModel.toggleWatchlist(asset) },
                    onClick = { viewModel.selectAsset(asset) }
                )
            }
        }

        item {
            Text("برترین‌های بازار", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
        }

        items(allAssets) { asset ->
            CryptoAssetCardPremium(
                asset,
                usdRateToman = usdRateToman,
                onToggleWatchlist = { viewModel.toggleWatchlist(asset) },
                onClick = { viewModel.selectAsset(asset) }
            )
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun CryptoAssetCardPremium(
    asset: CryptoAssetEntity, 
    usdRateToman: BigDecimal,
    onToggleWatchlist: () -> Unit, 
    onClick: () -> Unit
) {
    val priceToman = (asset.priceUsd ?: BigDecimal.ZERO).multiply(usdRateToman)
    val change = asset.percentChange24h ?: BigDecimal.ZERO
    val changeColor = if (change >= BigDecimal.ZERO) EmeraldCore else RoseCoral

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSlate800),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CryptoIcon(cmcId = asset.cmcId, symbol = asset.symbol, size = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(asset.symbol, style = DaraTypography.labelSmall, color = Slate400)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatUsd(asset.priceUsd ?: BigDecimal.ZERO), style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(
                    PersianNumberUtils.formatCurrency(priceToman, isRial = false) + " ت",
                    style = DaraTypography.labelSmall,
                    color = IndigoElectric
                )
                Text(
                    formatPercentSigned(change),
                    color = changeColor,
                    style = DaraTypography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onToggleWatchlist, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (asset.isInWatchlist) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (asset.isInWatchlist) RefinedAmberGold else Slate600
                )
            }
        }
    }
}
