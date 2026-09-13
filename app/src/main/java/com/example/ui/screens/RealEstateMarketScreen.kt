package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealEstateMarketScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val realEstates by viewModel.realEstates.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("املاک و مستغلات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }, enabled = !isRefreshing) {
                        if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = IndigoElectric)
                        else Icon(Icons.Default.Refresh, null, tint = IndigoElectric)
                    }
                }
            )
        }
    ) { padding ->
        if (realEstates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("داده‌ای برای املاک یافت نشد.", color = Slate600)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(realEstates) { property ->
                    RealEstateCard(property)
                }
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
