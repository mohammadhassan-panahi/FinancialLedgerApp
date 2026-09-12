package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNextStep: (com.example.data.local.PortfolioAssetType) -> Unit,
    onDirectAssetSelect: (String, com.example.data.local.PortfolioAssetType) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val allCrypto by viewModel.cryptoAssets.collectAsStateWithLifecycle()
    val allStocks by viewModel.watchlist.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
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
                        Text("ثبت دارایی جدید", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, null, tint = Slate400)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("چه دارایی جدیدی ثبت می‌کنید؟", style = DaraTypography.headlineMedium, color = Slate50, fontWeight = FontWeight.Black)
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجوی نماد (مثلاً BTC یا فولاد)...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate400) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoElectric,
                    unfocusedBorderColor = ObsidianSlate700
                )
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchQuery.isNotEmpty()) {
                    // Search Results
                    val cryptoResults = allCrypto.filter { it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true) }.take(5)
                    val stockResults = allStocks.filter { it.symbol.contains(searchQuery, true) || it.fullName.contains(searchQuery, true) }.take(5)
                    
                    if (cryptoResults.isNotEmpty()) {
                        item { Text("رمزارزها", style = DaraTypography.labelSmall, color = Slate400) }
                        items(cryptoResults) { crypto ->
                            SearchResultItem(name = crypto.name, symbol = crypto.symbol, color = IndigoElectric, icon = Icons.Default.CurrencyBitcoin) {
                                onDirectAssetSelect(crypto.symbol, PortfolioAssetType.CRYPTO)
                            }
                        }
                    }
                    
                    if (stockResults.isNotEmpty()) {
                        item { Text("بورس تهران", style = DaraTypography.labelSmall, color = Slate400) }
                        items(stockResults) { stock ->
                            SearchResultItem(name = stock.fullName, symbol = stock.symbol, color = Color(0xFF8B5CF6), icon = Icons.Default.Analytics) {
                                onDirectAssetSelect(stock.symbol, PortfolioAssetType.STOCK)
                            }
                        }
                    }
                    
                    item { HorizontalDivider(color = ObsidianSlate700, modifier = Modifier.padding(vertical = 8.dp)) }
                }

                // General Types
                item { Text("دسته‌بندی‌ها", style = DaraTypography.labelSmall, color = Slate400) }
                item {
                    AssetTypeSelectCard(
                        item = AssetTypeItem(PortfolioAssetType.GOLD, "طلا و مسکوکات", "سکه، شمش، طلای ۱۸ عیار و ...", Icons.Default.BrightnessLow, RefinedAmberGold),
                        onClick = { onNextStep(PortfolioAssetType.GOLD) }
                    )
                }
                item {
                    AssetTypeSelectCard(
                        item = AssetTypeItem(PortfolioAssetType.USD, "ارزهای خارجی", "دلار، یورو، درهم و اسکناس نقد", Icons.Default.Payments, EmeraldCore),
                        onClick = { onNextStep(PortfolioAssetType.USD) }
                    )
                }
                item {
                    AssetTypeSelectCard(
                        item = AssetTypeItem(PortfolioAssetType.CRYPTO, "رمزارزها", "بیت‌کوین، تتر و دارایی‌های دیجیتال", Icons.Default.CurrencyBitcoin, IndigoElectric),
                        onClick = { onNextStep(PortfolioAssetType.CRYPTO) }
                    )
                }
                item {
                    AssetTypeSelectCard(
                        item = AssetTypeItem(PortfolioAssetType.STOCK, "بورس تهران", "سهام، حق تقدم و صندوق‌های ETF", Icons.Default.QueryStats, Color(0xFF8B5CF6)),
                        onClick = { onNextStep(PortfolioAssetType.STOCK) }
                    )
                }
                item {
                    AssetTypeSelectCard(
                        item = AssetTypeItem(PortfolioAssetType.REAL_ESTATE, "املاک و مستغلات", "مسکونی، تجاری و زمین", Icons.Default.Home, Color(0xFFF97316)),
                        onClick = { onNextStep(PortfolioAssetType.REAL_ESTATE) }
                    )
                }
                item {
                    AssetTypeSelectCard(
                        item = AssetTypeItem(PortfolioAssetType.VEHICLE, "خودرو", "ماشین‌های داخلی و وارداتی", Icons.Default.DirectionsCar, Color(0xFF6B7280)),
                        onClick = { onNextStep(PortfolioAssetType.VEHICLE) }
                    )
                }
            }
            
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric)
                    Text(
                        "با ثبت دارایی‌های خود، دارا می‌تواند تنوع سبد سرمایه‌گذاری شما را تحلیل کرده و ریسک‌های احتمالی را هشدار دهد.",
                        style = DaraTypography.bodySmall,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(name: String, symbol: String, color: Color, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Column {
                Text(name, style = DaraTypography.bodyMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(symbol, style = DaraTypography.labelSmall, color = Slate400)
            }
        }
    }
}

data class AssetTypeItem(
    val type: PortfolioAssetType,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AssetTypeSelectCard(item: AssetTypeItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(item.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(item.desc, style = DaraTypography.labelSmall, color = Slate600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Slate600, modifier = Modifier.size(20.dp))
        }
    }
}
