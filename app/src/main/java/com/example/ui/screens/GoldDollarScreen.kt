package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MarketRateEntity
import com.example.data.local.PriceAlertEntity
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PriceAlertDialog
import com.example.ui.theme.*
import java.math.BigDecimal
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import com.example.util.priceRial

@Composable
fun GoldDollarScreen(viewModel: com.example.ui.viewmodel.PortfolioViewModel) {
    val allRates by viewModel.marketRates.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var alertTarget by remember { mutableStateOf<MarketRateEntity?>(null) }

    val hasLiveRates = allRates.any { !it.isOfflineRate }
    val rates = (if (hasLiveRates) allRates.filter { !it.isOfflineRate } else allRates)
        .filter { it.isOfflineRate || it.currency == "تومان" }

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
                Text("طلا، سکه و ارز", style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { viewModel.refreshAll() },
                    enabled = !isRefreshing
                ) { 
                    if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = IndigoElectric, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, null, tint = IndigoElectric)
                }
            }
        }
        
        if (isOffline) {
            item {
                Surface(
                    color = RoseCoral.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseCoral.copy(alpha = 0.2f))
                ) {
                    Text(
                        "اتصال برقرار نیست — نرخ‌های آخرین بروزرسانی نمایش داده می‌شوند.",
                        modifier = Modifier.padding(12.dp),
                        style = DaraTypography.bodySmall,
                        color = RoseCoral
                    )
                }
            }
        }
        
        if (rates.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IndigoElectric)
                }
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
    val isRial = LocalIsRial.current
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(rate.name, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(
                    formatPercentSigned(rate.changePercent),
                    style = DaraTypography.labelSmall,
                    color = if (rate.changePercent >= BigDecimal.ZERO) EmeraldCore else RoseCoral,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatRial(rate.priceRial, isRial = isRial),
                    style = DaraTypography.titleMedium,
                    color = Slate50,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSetAlert,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Notifications, null, tint = Slate400, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
