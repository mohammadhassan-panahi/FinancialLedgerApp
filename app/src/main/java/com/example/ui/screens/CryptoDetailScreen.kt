package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CryptoAssetEntity
import com.example.ui.components.CryptoIcon
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.util.PersianNumberUtils
import com.example.util.formatUsd

@Composable
fun CryptoDetailScreen(
    asset: CryptoAssetEntity,
    usdRateToman: Double,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            CryptoDetailHeader(asset = asset, onBack = onBack)
        },
        bottomBar = {
            CryptoStickyFooter()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Price Section
            PriceHeroSection(asset = asset, usdRateToman = usdRateToman)

            // Chart Section
            CandleChartCard()

            // Dara AI Score Section
            DaraScoreGaugeCard(score = 94)

            // Network Stats Grid
            NetworkStatsGrid(asset = asset)
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun CryptoDetailHeader(asset: CryptoAssetEntity, onBack: () -> Unit) {
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
                CryptoIcon(cmcId = asset.cmcId, symbol = asset.symbol, size = 32.dp)
                Column {
                    Text(asset.name, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text(asset.symbol, style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Grade, null, tint = RefinedAmberGold)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, null, tint = Slate400)
                }
            }
        }
    }
}

@Composable
fun PriceHeroSection(asset: CryptoAssetEntity, usdRateToman: Double) {
    val priceToman = (asset.priceUsd ?: 0.0) * usdRateToman
    val change = asset.percentChange24h ?: 0.0
    val changeColor = if (change >= 0) EmeraldCore else RoseCoral

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(formatUsd(asset.priceUsd ?: 0.0), style = DaraTypography.displayLarge, color = Slate50, fontWeight = FontWeight.Black)
                Text("USD", style = DaraTypography.labelMedium, color = Slate600, modifier = Modifier.padding(bottom = 8.dp))
            }
            
            Surface(color = changeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(percent = 100)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(if (change >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.TrendingDown, null, tint = changeColor, modifier = Modifier.size(14.dp))
                    Text("${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%", style = DaraTypography.labelMedium, color = changeColor, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.CurrencyExchange, null, tint = RefinedAmberGold, modifier = Modifier.size(16.dp))
            Text("معادل تقریبی:", style = DaraTypography.bodySmall, color = Slate400)
            Text(PersianNumberUtils.formatCurrency(priceToman, isRial = false), style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            Text("تومان", style = DaraTypography.labelSmall, color = Slate400)
        }
    }
}

@Composable
fun CandleChartCard() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(listOf("۱۵د", "۱س", "۴س", "۱ر", "۱هـ")) { time ->
                        val isSelected = time == "۴س"
                        Surface(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) IndigoElectric else ObsidianSlate600,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(time, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = DaraTypography.labelSmall, color = if (isSelected) Color.White else Slate400)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CandlestickChart, null, tint = Slate400, modifier = Modifier.size(20.dp))
                    Icon(Icons.Default.Fullscreen, null, tint = Slate400, modifier = Modifier.size(20.dp))
                }
            }
            
            // Mock Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(ObsidianSlate900.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Candlestick Chart API Placeholder", style = DaraTypography.labelSmall, color = Slate600)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IndicatorChip(label = "RSI (14):", value = "68.2", color = EmeraldCore)
                IndicatorChip(label = "MACD:", value = "گاوی", color = IndigoElectric)
            }
        }
    }
}

@Composable
fun IndicatorChip(label: String, value: String, color: Color) {
    Surface(color = ObsidianSlate700, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Text(label, style = DaraTypography.labelSmall, color = Slate400)
            Text(value, style = DaraTypography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DaraScoreGaugeCard(score: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800,
        border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                    Column {
                        Text("شاخص هوش مالی دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                        Text("Dara AI Momentum Score", style = DaraTypography.labelSmall, color = Slate400)
                    }
                }
                Surface(color = EmeraldCore.copy(alpha = 0.1f), shape = CircleShape) {
                    Text("خرید قوی", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = DaraTypography.labelSmall, color = EmeraldCore, fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = ObsidianSlate600,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(RefinedAmberGold, EmeraldCore, IndigoElectric)),
                            startAngle = 135f,
                            sweepAngle = (score / 100f) * 270f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(score.toString(), style = DaraTypography.displaySmall, color = Slate50, fontWeight = FontWeight.Black)
                }
                
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScoreFactorBar(label = "تحلیل تکنیکال", value = 0.92f, color = EmeraldCore)
                    ScoreFactorBar(label = "جریان درون‌شبکه", value = 0.96f, color = IndigoElectric)
                    ScoreFactorBar(label = "سنتیمنت اجتماعی", value = 0.91f, color = RefinedAmberGold)
                }
            }
        }
    }
}

@Composable
fun ScoreFactorBar(label: String, value: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = DaraTypography.labelSmall, color = Slate400)
            Text("${(value * 100).toInt()}%", style = DaraTypography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = color,
            trackColor = ObsidianSlate900
        )
    }
}

@Composable
fun NetworkStatsGrid(asset: CryptoAssetEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("آمار کلیدی بازار و شبکه", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(label = "ارزش کل بازار", value = formatUsd(asset.marketCapUsd ?: 0.0), icon = Icons.Default.PieChart, modifier = Modifier.weight(1f))
            StatTile(label = "حجم ۲۴ ساعته", value = formatUsd(asset.volume24hUsd ?: 0.0), icon = Icons.Default.BarChart, modifier = Modifier.weight(1f))
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(label = "سقف تاریخی", value = "$93,480", icon = Icons.Default.WorkspacePremium, modifier = Modifier.weight(1f))
            StatTile(label = "دامیننس", value = "۵۸.۴٪", icon = Icons.Default.DonutLarge, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatTile(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = Slate600, modifier = Modifier.size(14.dp))
                Text(label, style = DaraTypography.labelSmall, color = Slate400)
            }
            Text(value, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun CryptoStickyFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        color = ObsidianSlate800.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp
    ) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldCore)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Bolt, null)
                    Text("معامله و خرید سریع", style = DaraTypography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                onClick = { },
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSlate700
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.NotificationsActive, null, tint = Slate50)
                }
            }
        }
    }
}
