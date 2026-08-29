package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LocalIsRial
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InflationCalculatorScreen(viewModel: PortfolioViewModel) {
    val isRial = LocalIsRial.current
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val totalRealGrowth = holdings.sumOf { it.inflationAdjustedProfitLossRial }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("محاسبه‌گر رشد واقعی (تورم)") })
            },
            containerColor = DarkSlateSurface
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("مجموع رشد ثروت (پس از کسر تورم)", color = TextSecondary, fontSize = 14.sp)
                            Text(
                                formatRial(totalRealGrowth, isRial = isRial),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (totalRealGrowth >= 0) EmeraldProfit else RoseLoss
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "این رقم نشان‌دهنده تغییر قدرت خرید شما از زمان خرید دارایی‌هاست.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(holdings) { holding ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(holding.assetName, color = TextPrimary)
                        Text(
                            formatRial(holding.inflationAdjustedProfitLossRial, isRial = isRial),
                            color = if (holding.inflationAdjustedProfitLossRial >= 0) EmeraldProfit else RoseLoss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(color = SlateBorderLight)
                }
            }
        }
    }
}
