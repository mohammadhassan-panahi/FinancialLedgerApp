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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.PersianNumberUtils
import java.math.BigDecimal

@Composable
fun CalculatorsHubScreen(
    viewModel: CalculatorViewModel,
    goldPriceToman: BigDecimal,
    onBack: () -> Unit,
    onNavigateToCurrencyConverter: () -> Unit,
    onNavigateToCryptoConverter: () -> Unit,
    onNavigateToCompoundInterest: () -> Unit
) {
    var goldWeight by remember { mutableStateOf("1") }
    
    val weightValue = PersianNumberUtils.parseAmount(goldWeight)
    val totalGoldValue = weightValue.multiply(goldPriceToman)

    Scaffold(
        topBar = { CalculatorHeader(onBack = onBack) },
        containerColor = ObsidianSlate900
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Gold Instant Calculator
            item {
                GoldCalculatorCard(
                    weight = goldWeight,
                    onWeightChange = { goldWeight = it },
                    totalValue = totalGoldValue
                )
            }

            // Categories
            item {
                Text(
                    "ابزارهای مالی دارا",
                    style = DaraTypography.titleLarge,
                    color = Slate50,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryToolCard(
                        "مبدل واحد پولی",
                        "تبدیل آنی ریال و تومان با دقت بالا",
                        Icons.Default.CurrencyExchange,
                        IndigoElectric,
                        onClick = onNavigateToCurrencyConverter
                    )
                    SecondaryToolCard(
                        "سود مرکب",
                        "محاسبه رشد سرمایه در بلندمدت",
                        Icons.Default.TrendingUp,
                        EmeraldCore,
                        onClick = onNavigateToCompoundInterest
                    )
                    SecondaryToolCard(
                        "محاسبه حباب طلا",
                        "تشخیص اختلاف قیمت بازار با ارزش ذاتی",
                        Icons.Default.BubbleChart,
                        RefinedAmberGold,
                        onClick = { /* TODO */ }
                    )
                    SecondaryToolCard(
                        "اقساط وام",
                        "برنامه‌ریزی بازپرداخت تسهیلات",
                        Icons.Default.AccountBalance,
                        Slate400,
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorHeader(onBack: () -> Unit) {
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
                    Text("دارا Toolkit", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("موتور محاسباتی هوشمند", style = DaraTypography.labelSmall, color = IndigoElectric)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Notifications, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun GoldCalculatorCard(
    weight: String,
    onWeightChange: (String) -> Unit,
    totalValue: BigDecimal
) {
    DaraGlassCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Balance, null, tint = RefinedAmberGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("محاسبه آنی ارزش طلا (۱۸ عیار)", style = DaraTypography.labelMedium, color = Slate400)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PersianNumberTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = "وزن (گرم)",
                    modifier = Modifier.weight(1f)
                )
                
                Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.End) {
                    Text(
                        PersianNumberUtils.formatCurrency(totalValue),
                        style = DaraTypography.titleLarge,
                        color = Slate50,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("ارزش کل روز", style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CaratChip("۱۸ عیار", true, Modifier.weight(1f))
                CaratChip("۲۴ عیار", false, Modifier.weight(1f))
                CaratChip("انس", false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CaratChip(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) RefinedAmberGold.copy(alpha = 0.2f) else ObsidianSlate700,
        border = BorderStroke(1.dp, if (selected) RefinedAmberGold else Color.Transparent)
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            style = DaraTypography.labelSmall,
            color = if (selected) RefinedAmberGold else Slate400
        )
    }
}

@Composable
fun SecondaryToolCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    DaraGlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.1f)
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text(desc, style = DaraTypography.bodySmall, color = Slate400)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = Slate400)
        }
    }
}
