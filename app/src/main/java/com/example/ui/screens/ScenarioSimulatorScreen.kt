package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianNumberUtils

@Composable
fun ScenarioSimulatorScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val summary by viewModel.portfolioSummary.collectAsStateWithLifecycle(null)
    val holdings by viewModel.holdings.collectAsStateWithLifecycle(emptyList())

    var goldChange by remember { mutableStateOf(20f) }
    var usdChange by remember { mutableStateOf(-10f) }
    var stockChange by remember { mutableStateOf(15f) }
    var cryptoChange by remember { mutableStateOf(35f) }

    // Real base values from user portfolio
    val baseWorth = (summary?.totalValueRial ?: 0.0) / 10.0 // To Toman
    
    var goldBase = 0.0
    var stockBase = 0.0
    var usdBase = 0.0
    var cryptoBase = 0.0
    
    val iterator = holdings.iterator()
    while (iterator.hasNext()) {
        val holding = iterator.next()
        when (holding.assetType) {
            PortfolioAssetType.GOLD -> goldBase += holding.currentValueRial / 10.0
            PortfolioAssetType.STOCK -> stockBase += holding.currentValueRial / 10.0
            PortfolioAssetType.USD -> usdBase += holding.currentValueRial / 10.0
            PortfolioAssetType.CRYPTO -> cryptoBase += holding.currentValueRial / 10.0
            else -> {}
        }
    }
    
    val otherBase = baseWorth - (goldBase + stockBase + usdBase + cryptoBase)

    val goldSim = goldBase * (1 + goldChange / 100)
    val stockSim = stockBase * (1 + stockChange / 100)
    val usdSim = usdBase * (1 + usdChange / 100)
    val cryptoSim = cryptoBase * (1 + cryptoChange / 100)

    val totalProjected = goldSim + stockSim + usdSim + cryptoSim + otherBase
    val delta = totalProjected - baseWorth
    val percentDelta = if (baseWorth > 0) ((delta / baseWorth) * 100).toFloat() else 0f

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            SimulatorHeader(onBack = onBack)
        }
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Engine Status
            item {
                EngineStatusStrip()
            }

            // Section 2: Current Wealth (Real)
            item {
                DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ارزش فعلی سرمایه شما (تومان)", style = DaraTypography.labelSmall, color = Slate400)
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(PersianNumberUtils.formatDecimal(baseWorth), style = DaraTypography.displaySmall, color = Slate50, fontWeight = FontWeight.Black)
                            Text("تومان", style = DaraTypography.titleMedium, color = Slate600)
                        }
                    }
                }
            }

            // Section 3: Projected Net Worth
            item {
                ProjectedWealthCard(totalProjected, percentDelta, delta)
            }

            // Section 4: Sliders
            item {
                Text("متغیرهای بازار (سناریو)", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MarketVariableSlider(
                        label = "طلا و سکه امامی",
                        value = goldChange,
                        onValueChange = { goldChange = it },
                        icon = Icons.Default.MonetizationOn,
                        color = RefinedAmberGold,
                        range = -30f..50f
                    )
                    MarketVariableSlider(
                        label = "نرخ دلار و تتر",
                        value = usdChange,
                        onValueChange = { usdChange = it },
                        icon = Icons.Default.CurrencyExchange,
                        color = EmeraldCore,
                        range = -20f..40f
                    )
                    MarketVariableSlider(
                        label = "بورس تهران",
                        value = stockChange,
                        onValueChange = { stockChange = it },
                        icon = Icons.Default.CandlestickChart,
                        color = IndigoElectric,
                        range = -25f..35f
                    )
                }
            }

            // Section 5: AI Strategy
            item {
                DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.SmartToy, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("استراتژی پیشنهادی دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                            val advice = when {
                                percentDelta > 10 -> "در این سناریو، بازدهی شما بسیار مطلوب است. پیشنهاد می‌شود بخشی از سود را به دارایی‌های کم‌ریسک منتقل کنید."
                                percentDelta < 0 -> "در صورت وقوع این سناریو، پورتفوی شما با کاهش ارزش مواجه می‌شود. افزایش سهم طلا می‌تواند به عنوان پوشش ریسک عمل کند."
                                else -> "تنوع‌بخشی فعلی شما مناسب است. حفظ آرامش و پایش مداوم نرخ‌ها توصیه می‌شود."
                            }
                            Text(
                                advice,
                                style = DaraTypography.bodySmall,
                                color = Slate400,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SimulatorHeader(onBack: () -> Unit) {
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
                Text("شبیه‌ساز سناریو", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.RestartAlt, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun EngineStatusStrip() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSlate800.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(EmeraldCore, CircleShape))
                Text("موتور هوشمند v4.2 فعال", style = DaraTypography.labelSmall, color = EmeraldCore)
            }
            Text("به‌روزرسانی: آنی", style = DaraTypography.labelSmall, color = Slate600)
        }
    }
}

@Composable
fun ProjectedWealthCard(total: Double, percent: Float, delta: Double) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("ارزش برآوردی ثروت در افق سناریو", style = DaraTypography.labelSmall, color = Slate400)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(PersianNumberUtils.formatDecimal(total), style = DaraTypography.displaySmall, color = IndigoElectric, fontWeight = FontWeight.Black)
                    Text("تومان", style = DaraTypography.titleMedium, color = Slate600)
                }
                Surface(color = EmeraldCore.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "${if (percent >= 0) "+" else ""}${String.format("%.2f", percent)}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = DaraTypography.labelLarge,
                        color = EmeraldCore,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider(color = GlassBorderLight)
            Text(
                "اختلاف خالص: ${if (delta >= 0) "+" else ""}${PersianNumberUtils.formatDecimal(delta)} تومان",
                style = DaraTypography.bodySmall,
                color = Slate400
            )
        }
    }
}

@Composable
fun MarketVariableSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    color: Color,
    range: ClosedFloatingPointRange<Float>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSlate800.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                    Text(label, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                }
                Text(
                    "${if (value >= 0) "+" else ""}${value.toInt()}٪",
                    style = DaraTypography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = ObsidianSlate600
                )
            )
        }
    }
}
