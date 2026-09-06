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
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*

@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onAssetClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            SearchHeader(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Filter Pills
            item {
                SearchFilterPills()
            }

            // Results: Assets
            item {
                SearchSectionHeader(title = "دارایی‌ها و نمادها", count = 5)
            }
            items(3) {
                SearchResultAssetCard(
                    title = if (it == 0) "بیت‌کوین" else "طلای ۱۸ عیار",
                    code = if (it == 0) "BTC" else "GOLD",
                    price = if (it == 0) "۶,۴۸۰,۰۰۰,۰۰۰" else "۴,۴۵۰,۰۰۰",
                    change = "+۲.۱٪",
                    icon = if (it == 0) Icons.Default.CurrencyBitcoin else Icons.Default.MonetizationOn,
                    color = if (it == 0) IndigoElectric else RefinedAmberGold,
                    onClick = { onAssetClick("BTC") }
                )
            }

            // Results: AI Insights
            item {
                SearchSectionHeader(title = "تحلیل‌های هوش مصنوعی", count = 4)
            }
            items(2) {
                SearchResultAiCard()
            }

            // Results: Tools
            item {
                SearchSectionHeader(title = "جعبه ابزار مالی", count = 3)
            }
            item {
                SearchResultToolCard(
                    title = "محاسبه‌گر حباب طلا",
                    desc = "محاسبه سریع حباب سکه و اجرت ساخت",
                    icon = Icons.Default.Calculate,
                    color = RefinedAmberGold
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate50)
            }
            
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("جستجوی نماد، ارز یا طلا...", style = DaraTypography.bodyMedium, color = Slate600) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = IndigoElectric) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, null, tint = Slate600)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ObsidianSlate800,
                    unfocusedContainerColor = ObsidianSlate800,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Slate50
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IndigoElectric
            ) {
                Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
            }
        }
    }
}

@Composable
fun SearchFilterPills() {
    val filters = listOf("همه نتایج", "دارایی‌ها", "تحلیل هوشمند", "ابزارها")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            val isSelected = filter == "همه نتایج"
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
fun SearchSectionHeader(title: String, count: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.width(3.dp).height(16.dp).background(IndigoElectric, CircleShape))
            Text(title, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
        }
        Text("مشاهده $count مورد", style = DaraTypography.labelSmall, color = IndigoElectric)
    }
}

@Composable
fun SearchResultAssetCard(
    title: String,
    code: String,
    price: String,
    change: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    Surface(color = IndigoElectric.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(code, modifier = Modifier.padding(horizontal = 4.dp), style = DaraTypography.labelSmall, color = IndigoElectric)
                    }
                }
                Text("بازار مالی • نقدی", style = DaraTypography.labelSmall, color = Slate600)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(price, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(change, style = DaraTypography.labelSmall, color = EmeraldCore, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SearchResultAiCard() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(color = RefinedAmberGold.copy(alpha = 0.1f), shape = CircleShape) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Psychology, null, tint = RefinedAmberGold, modifier = Modifier.size(12.dp))
                        Text("تحلیل هوشمند", style = DaraTypography.labelSmall, color = RefinedAmberGold)
                    }
                }
                Text("۵ دقیقه پیش", style = DaraTypography.labelSmall, color = Slate600)
            }
            Text("سناریوی عبور بیت‌کوین از مقاومت ۹۵ هزار دلار و اثر بر نرخ تتر", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
            Text("مشاهده تحلیل کامل", style = DaraTypography.labelSmall, color = IndigoElectric, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SearchResultToolCard(title: String, desc: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text(desc, style = DaraTypography.labelSmall, color = Slate600, maxLines = 1)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = Slate600)
        }
    }
}
