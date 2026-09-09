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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BankAccountEntity
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.util.PersianNumberUtils
import com.example.util.formatRial
import java.math.BigDecimal

@Composable
fun BankAccountsScreen(
    viewModel: com.example.ui.viewmodel.PortfolioViewModel,
    onBack: () -> Unit
) {
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val totalLiquidity by viewModel.totalLiquidityToman.collectAsStateWithLifecycle()
    var isBalanceVisible by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            BankHeader(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Total Liquidity Card
            item {
                TotalLiquidityCard(
                    totalToman = totalLiquidity,
                    isVisible = isBalanceVisible,
                    onToggleVisibility = { isBalanceVisible = !isBalanceVisible }
                )
            }

            // Section 2: Bank Cards Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("کارت‌های متصل و سپرده‌ها", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { /* TODO: Add Card */ }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = IndigoElectric)
                            Text("افزودن", color = IndigoElectric, style = DaraTypography.labelLarge)
                        }
                    }
                    
                    if (bankAccounts.isEmpty()) {
                        EmptyBankCard()
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(bankAccounts) { account ->
                                PremiumBankCard(account)
                            }
                        }
                    }
                }
            }

            // Section 3: Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BankQuickAction(icon = Icons.Default.SwapHoriz, label = "کارت به کارت", color = IndigoElectric)
                    BankQuickAction(icon = Icons.Default.Send, label = "پایا و ساتنا", color = EmeraldCore)
                    BankQuickAction(icon = Icons.Default.Percent, label = "سود سپرده", color = RefinedAmberGold)
                    BankQuickAction(icon = Icons.Default.Tune, label = "سقف برداشت", color = Slate400)
                }
            }

            // Section 4: Transaction Ledger (Mocked for UI)
            item {
                Text("گردش حساب و تراکنش‌ها", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            
            items(5) {
                MockTransactionItem()
            }
        }
    }
}

@Composable
fun BankHeader(onBack: () -> Unit) {
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
                    Text("مدیریت نقدینگی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("بانکداری یکپارچه دارا", style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Security, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun TotalLiquidityCard(
    totalToman: BigDecimal,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val isRial = LocalIsRial.current
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    shape = RoundedCornerShape(percent = 100),
                    color = ObsidianSlate600.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(EmeraldCore, CircleShape))
                        Text("موجودی تجمیعی شتاب", style = DaraTypography.labelSmall, color = EmeraldCore)
                    }
                }
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                    Icon(if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Slate400)
                }
            }

            Column {
                Text("مجموع نقدینگی در دسترس", style = DaraTypography.labelSmall, color = Slate400)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isVisible) PersianNumberUtils.formatDecimal(totalToman) else "••••••••",
                        style = DaraTypography.displayLarge,
                        color = Slate50,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(if (isRial) "ریال" else "تومان", style = DaraTypography.titleMedium, color = IndigoElectric, modifier = Modifier.padding(bottom = 6.dp))
                }
            }

            HorizontalDivider(color = GlassBorderLight)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { }) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp), tint = IndigoElectric)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("کپی شبا متمرکز", style = DaraTypography.labelSmall, color = IndigoElectric)
                }
                TextButton(onClick = { }) {
                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(14.dp), tint = Slate400)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("صورت‌حساب", style = DaraTypography.labelSmall, color = Slate400)
                }
            }
        }
    }
}

@Composable
fun PremiumBankCard(account: BankAccountEntity) {
    val cardColor = try { Color(android.graphics.Color.parseColor(account.colorHex)) } catch (e: Exception) { IndigoElectric }
    
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(cardColor.copy(alpha = 0.8f), ObsidianSlate800)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(account.bankName, style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
                    Text(account.name, style = DaraTypography.labelSmall, color = Slate400)
                }
                Icon(Icons.Default.AccountBalance, null, tint = cardColor, modifier = Modifier.size(28.dp))
            }
            
            Text(
                "۶۰۳۷ •••• •••• ۸۴۱۲", // Mock number for now
                style = DaraTypography.titleLarge,
                color = Slate50,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("موجودی", style = DaraTypography.labelSmall, color = Slate400)
                    Text(
                        "${PersianNumberUtils.formatDecimal(account.currentBalance)} تومان",
                        style = DaraTypography.titleMedium,
                        color = Slate50,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("۰۷/۲۷", style = DaraTypography.labelMedium, color = Slate50)
            }
        }
    }
}

@Composable
fun EmptyBankCard() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.CreditCard, null, tint = Slate600, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("هیچ کارتی متصل نیست", color = Slate400, style = DaraTypography.bodyMedium)
        }
    }
}

@Composable
fun BankQuickAction(icon: ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = ObsidianSlate800,
            onClick = { }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = DaraTypography.labelSmall, color = Slate50)
    }
}

@Composable
fun MockTransactionItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSlate800.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(EmeraldCore.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.TrendingUp, null, tint = EmeraldCore, modifier = Modifier.size(20.dp))
            }
            Column {
                Text("سود ماهانه سپرده", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text("بانک سامان", style = DaraTypography.labelSmall, color = Slate400)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("+۱,۳۸۰,۰۰۰", style = DaraTypography.titleMedium, color = EmeraldCore, fontWeight = FontWeight.Bold)
            Text("امروز، ۰۸:۰۰", style = DaraTypography.labelSmall, color = Slate600)
        }
    }
}
