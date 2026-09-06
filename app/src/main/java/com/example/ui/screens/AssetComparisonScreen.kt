package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*

@Composable
fun AssetComparisonScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            ComparisonHeader(onBack = onBack)
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
            // Asset Selection Card
            ComparisonAssetSelector()

            // Comparison Chart
            ComparisonChartCard()

            // Detailed Stats Table
            ComparisonStatsTable()

            // AI Verdict
            DaraAiVerdictCard()
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ComparisonHeader(onBack: () -> Unit) {
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
                Column {
                    Text("مقایسه دارایی‌ها", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("پایش لحظه‌ای بازار", style = DaraTypography.labelSmall, color = EmeraldCore)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Tune, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun ComparisonAssetSelector() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AssetCompareItem(
                    title = "طلای ۱۸ عیار",
                    price = "۴,۵۲۰,۰۰۰",
                    change = "+۲.۴٪",
                    icon = Icons.Default.MonetizationOn,
                    color = RefinedAmberGold,
                    modifier = Modifier.weight(1f)
                )
                AssetCompareItem(
                    title = "بیت‌کوین (BTC)",
                    price = "$67,450",
                    change = "+۵.۸٪",
                    icon = Icons.Default.CurrencyBitcoin,
                    color = IndigoElectric,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // VS Badge
            Surface(
                modifier = Modifier.align(Alignment.Center).size(36.dp),
                shape = CircleShape,
                color = ObsidianSlate800,
                border = BorderStroke(2.dp, ObsidianSlate900)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("VS", style = DaraTypography.labelSmall, color = IndigoElectric, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun AssetCompareItem(title: String, price: String, change: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSlate600.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                }
                Text(title, style = DaraTypography.labelSmall, color = Slate50, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Text(price, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            Text(change, style = DaraTypography.labelSmall, color = EmeraldCore, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ComparisonChartCard() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("بازدهی تجمعی ۶ ماهه", style = DaraTypography.labelSmall, color = Slate400)
                    Text("+۱۸.۴٪ اختلاف", style = DaraTypography.titleMedium, color = IndigoElectric, fontWeight = FontWeight.Bold)
                }
                Surface(color = ObsidianSlate700, shape = RoundedCornerShape(8.dp)) {
                    Text("ROI %", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            
            // Mock Multi-line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(ObsidianSlate900.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Multi-Line Comparative Chart", style = DaraTypography.labelSmall, color = Slate600)
            }
        }
    }
}

@Composable
fun ComparisonStatsTable() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("جدول رقابتی شاخص‌ها", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSlate800.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ComparisonRow(label = "نوسان‌پذیری", val1 = "۱۴.۸٪", val2 = "۴۲.۵٪")
                HorizontalDivider(color = ObsidianSlate700, modifier = Modifier.padding(horizontal = 16.dp))
                ComparisonRow(label = "همبستگی با دلار", val1 = "۹۱٪", val2 = "۴۵٪")
                HorizontalDivider(color = ObsidianSlate700, modifier = Modifier.padding(horizontal = 16.dp))
                ComparisonRow(label = "امتیاز دارا", val1 = "۸۸", val2 = "۹۲", isHighlight = true)
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, val1: String, val2: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHighlight) IndigoElectric.copy(alpha = 0.05f) else Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(val1, modifier = Modifier.weight(1f), style = DaraTypography.labelLarge, color = if (isHighlight) EmeraldCore else Slate50, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(label, modifier = Modifier.weight(1f), style = DaraTypography.labelSmall, color = Slate400, textAlign = TextAlign.Center)
        Text(val2, modifier = Modifier.weight(1f), style = DaraTypography.labelLarge, color = if (isHighlight) RefinedAmberGold else Slate50, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun DaraAiVerdictCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate800,
        border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(36.dp).background(IndigoElectric.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Psychology, null, tint = IndigoElectric)
                }
                Text("دیدگاه استراتژیک هوشمند دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Text(
                "در افق ۶ ماهه، بیت‌کوین بازدهی اسمی بالاتری داشته اما طلا به دلیل نوسان کمتر، نسبت شارپ برتری برای ریال ثبت کرده است.",
                style = DaraTypography.bodyMedium,
                color = Slate400,
                lineHeight = 22.sp
            )
            
            Surface(color = ObsidianSlate900, shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("توزیع پیشنهادی: ۶۰٪ طلا / ۴۰٪ بیت‌کوین", style = DaraTypography.labelSmall, color = IndigoElectric, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(ObsidianSlate700)) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxHeight().weight(0.6f).background(EmeraldCore))
                            Box(modifier = Modifier.fillMaxHeight().weight(0.4f).background(RefinedAmberGold))
                        }
                    }
                }
            }
        }
    }
}
