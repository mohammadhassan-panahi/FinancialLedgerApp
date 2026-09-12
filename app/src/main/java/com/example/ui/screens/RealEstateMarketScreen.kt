package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RealEstateEntity
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import java.math.BigDecimal

@Composable
fun RealEstateMarketScreen(viewModel: PortfolioViewModel) {
    val realEstates by viewModel.realEstates.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

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
                Text("املاک و مستغلات", style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.refreshAll() }, enabled = !isRefreshing) { 
                    if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = IndigoElectric)
                    else Icon(Icons.Default.Refresh, null, tint = IndigoElectric)
                }
            }
        }
        
        if (realEstates.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("داده‌ای برای املاک ثبت نشده است.", color = Slate600)
                }
            }
        } else {
            items(realEstates) { property ->
                RealEstateCard(property)
            }
        }
    }
}

@Composable
private fun RealEstateCard(property: RealEstateEntity) {
    val isRial = LocalIsRial.current
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(property.propertyName, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(
                    formatPercentSigned(property.changePercent),
                    style = DaraTypography.labelSmall,
                    color = if (property.changePercent >= BigDecimal.ZERO) EmeraldCore else RoseCoral,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                formatRial(property.valuationRial, isRial = isRial),
                style = DaraTypography.titleMedium,
                color = Slate50,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
