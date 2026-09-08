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
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.PersianNumberUtils

@Composable
fun CalculatorsHubScreen(
    viewModel: CalculatorViewModel,
    goldPriceToman: Double,
    onBack: () -> Unit,
    onNavigateToCurrencyConverter: () -> Unit,
    onNavigateToCryptoConverter: () -> Unit,
    onNavigateToCompoundInterest: () -> Unit
) {
    var goldWeight by remember { mutableStateOf("1") }
    
    val weightValue = PersianNumberUtils.parseAmount(goldWeight)
    val totalGoldValue = weightValue * goldPriceToman

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            CalculatorHeader(onBack = onBack)
        }
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = ObsidianSlate800.copy(alpha = 0.6f), shape = CircleShape) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(EmeraldCore, CircleShape))
                            Text("محاسبات بلادرنگ و تبدیل هوشمند", style = DaraTypography.labelSmall, color = Slate400)
                        }
                    }
                    Text("جعبه ابزار مالی دارا", style = DaraTypography.headlineLarge, color = Slate50, fontWeight = FontWeight.Bold)
                }
            }

            item {
                GoldCalculatorCard(
                    weight = goldWeight,
                    onWeightChange = { goldWeight = it },
                    totalValue = totalGoldValue
                )
            }

            item {
                Text("ابزارهای محاسباتی هوشمند", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SecondaryToolCard(
                        title = "مبدل ارزها", 
                        desc = "دلار، یورو و درهم", 
                        icon = Icons.Default.CurrencyExchange, 
                        color = EmeraldCore, 
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCurrencyConverter
                    )
                    SecondaryToolCard(
                        title = "رمزارز به تومان", 
                        desc = "تتر و بیت‌کوین", 
                        icon = Icons.Default.CurrencyBitcoin, 
                        color = IndigoElectric, 
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCryptoConverter
                    )
                }
            }

            item {
                Surface(
                    onClick = onNavigateToCompoundInterest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianSlate800.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(44.dp).background(EmeraldCore.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Percent, null, tint = EmeraldCore)
                            }
                            Column {
                                Text("سود سپرده مرکب", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                                Text("سود مؤثر سالانه ۳۱.۵٪", style = DaraTypography.labelSmall, color = Slate600)
                            }
                        }
                        Icon(Icons.Default.ChevronLeft, null, tint = Slate600)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
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
fun GoldCalculatorCard(weight: String, onWeightChange: (String) -> Unit, totalValue: Double) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(40.dp).background(RefinedAmberGold.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MonetizationOn, null, tint = RefinedAmberGold)
                }
                Column {
                    Text("ماشین‌حساب پیشرفته طلا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("محاسبه حباب، مظنه و مالیات", style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            
            PersianNumberTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = "وزن طلا (گرم)",
                isDecimalAllowed = true
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("عیار و استاندارد طلا", style = DaraTypography.labelSmall, color = Slate600)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CaratChip(label = "۱۸ عیار", isSelected = true, modifier = Modifier.weight(1f))
                    CaratChip(label = "۲۴ عیار", isSelected = false, modifier = Modifier.weight(1f))
                }
            }
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ObsidianSlate800.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ارزش تمام شده کل (تخمین)", style = DaraTypography.labelSmall, color = Slate400)
                    Text(PersianNumberUtils.formatCurrency(totalValue, showSuffix = true), style = DaraTypography.headlineSmall, color = Slate50, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun CaratChip(label: String, isSelected: Boolean, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) IndigoElectric.copy(alpha = 0.2f) else ObsidianSlate800,
        border = BorderStroke(1.dp, if (isSelected) IndigoElectric else Color.Transparent)
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Text(label, style = DaraTypography.labelMedium, color = if (isSelected) IndigoElectric else Slate400, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SecondaryToolCard(title: String, desc: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    DaraGlassCard(modifier = modifier.height(140.dp).clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Column {
                Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text(desc, style = DaraTypography.labelSmall, color = Slate600, maxLines = 1)
            }
        }
    }
}
