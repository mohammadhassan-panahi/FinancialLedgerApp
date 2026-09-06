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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CryptoAssetEntity
import com.example.ui.components.CryptoIcon
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.util.formatUsd

@Composable
fun CryptoIntelligenceScreen(
    onBack: () -> Unit,
    onAssetClick: (CryptoAssetEntity) -> Unit
) {
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            IntelligenceHeader(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Hero Intelligence
            item {
                IntelligenceHeroCard()
            }

            // Section 2: Macro Stats Strip
            item {
                MacroStatsRow()
            }

            // Section 3: Sector Filters
            item {
                SectorFiltersRow()
            }

            // Section 4: AI Recommendations
            item {
                AiAllocationCallout()
            }

            // Section 5: Intelligence Asset List
            item {
                Text("دارایی‌های منتخب هوشمند", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            
            // Mock items representing the top assets with AI scores
            items(5) {
                IntelligenceAssetCard(onClick = { /* Navigate to detail */ })
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun IntelligenceHeader(onBack: () -> Unit) {
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
                    Text("مرکز هوش کریپتو", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("Dara AI Live Intel", style = DaraTypography.labelSmall, color = IndigoElectric)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Tune, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun IntelligenceHeroCard() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(40.dp).background(IndigoElectric.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Psychology, null, tint = IndigoElectric)
                }
                Column {
                    Text("تحلیل هوش مصنوعی دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("پایش آن‌چین و بنیادی لحظه‌ای", style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            Text(
                "الگوریتم‌های دارا هم‌اکنون در حال رصد ۱۲۰۰ دارایی دیجیتال برای شناسایی فرصت‌های ورود کم‌ریسک هستند.",
                style = DaraTypography.bodySmall,
                color = Slate400,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun MacroStatsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MacroStatItem(label = "ترس و طمع", value = "۷۶", color = EmeraldCore, modifier = Modifier.weight(1f))
        MacroStatItem(label = "حجم معاملات", value = "$108B", color = Slate50, modifier = Modifier.weight(1f))
        MacroStatItem(label = "دامیننس BTC", value = "۵۴.۲٪", color = RefinedAmberGold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun MacroStatItem(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = DaraTypography.labelSmall, color = Slate600)
            Text(value, style = DaraTypography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectorFiltersRow() {
    val filters = listOf("همه", "لایه‌اول", "هوش مصنوعی", "دیفای", "بیشترین بازده")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            val isSelected = filter == "همه"
            Surface(
                shape = CircleShape,
                color = if (isSelected) IndigoElectric else ObsidianSlate800,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = DaraTypography.labelSmall,
                    color = if (isSelected) Color.White else Slate400,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AiAllocationCallout() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = IndigoElectric.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoGraph, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                Text("پیشنهاد تخصیص هفته جاری", style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Text(
                "با توجه به شکست مقاومت بیت‌کوین، افزایش سهم رمزارز پورتفو به ۱۵٪ توصیه می‌شود.",
                style = DaraTypography.bodySmall,
                color = Slate400,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun IntelligenceAssetCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Mock BTC Icon
            Box(modifier = Modifier.size(44.dp).background(RefinedAmberGold.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text("₿", color = RefinedAmberGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("بیت‌کوین", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("BTC", style = DaraTypography.labelSmall, color = Slate600)
                }
                Text("رتبه ۱ بازار", style = DaraTypography.labelSmall, color = Slate600)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$91,420", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = EmeraldCore, modifier = Modifier.size(12.dp))
                    Text("+۴.۸٪", style = DaraTypography.labelSmall, color = EmeraldCore, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            // AI Score Mini Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("۹۴", style = DaraTypography.labelLarge, color = EmeraldCore, fontWeight = FontWeight.Black)
                Text("هوش دارا", style = DaraTypography.labelSmall, color = Slate600, fontSize = 8.sp)
            }
        }
    }
}
