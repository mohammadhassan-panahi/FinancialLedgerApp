package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatRial
import java.math.BigDecimal

@Composable
fun MarketDashboardScreen(
    viewModel: PortfolioViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
    
    val usdRate = marketRates.find { it.assetCode == "USD" }?.priceToman ?: BigDecimal("65000")
    val goldRate = marketRates.find { it.assetCode.contains("GOLD_18K") || it.name.contains("۱۸") }?.priceToman ?: BigDecimal("3500000")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSlate900)
            .padding(16.dp)
    ) {
        Text(
            "مرکز کنترل بازار",
            style = DaraTypography.headlineSmall,
            color = Slate50,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 1. Key Rates Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MarketSummaryMiniCard(
                label = "دلار بازار آزاد",
                price = formatRial(usdRate.multiply(BigDecimal("10")), isRial = false),
                color = EmeraldCore,
                modifier = Modifier.weight(1f)
            )
            MarketSummaryMiniCard(
                label = "طلای ۱۸ عیار",
                price = formatRial(goldRate.multiply(BigDecimal("10")), isRial = false),
                color = RefinedAmberGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "دسترسی سریع",
            style = DaraTypography.titleMedium,
            color = Slate400,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2. Shortcut Grid
        val shortcuts = listOf(
            MarketShortcut("بورس تهران", Icons.Default.Analytics, Color(0xFF8B5CF6), "stock"),
            MarketShortcut("طلا و ارز", Icons.Default.MonetizationOn, RefinedAmberGold, "gold_fx"),
            MarketShortcut("رمزارزها", Icons.Default.CurrencyBitcoin, IndigoElectric, "crypto"),
            MarketShortcut("مبدل پیشرفته", Icons.Default.CurrencyExchange, EmeraldCore, "multi_converter"),
            MarketShortcut("قیمت خودرو", Icons.Default.DirectionsCar, Color(0xFF6B7280), "cars"),
            MarketShortcut("املاک", Icons.Default.Home, Color(0xFFF97316), "real_estate")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shortcuts) { item ->
                MarketShortcutCard(item = item) { onNavigateToSection(item.route) }
            }
        }
    }
}

@Composable
fun MarketSummaryMiniCard(label: String, price: String, color: Color, modifier: Modifier) {
    DaraGlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = DaraTypography.labelSmall, color = Slate400)
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, style = DaraTypography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}

data class MarketShortcut(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun MarketShortcutCard(item: MarketShortcut, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                item.title,
                style = DaraTypography.labelLarge,
                color = Slate50,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
