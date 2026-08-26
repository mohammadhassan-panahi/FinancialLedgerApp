package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.HoldingSummary
import com.example.ui.components.DonutSlice
import com.example.ui.components.PortfolioDonutChart
import com.example.ui.components.SellAssetDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial

private val GoldColor = Color(0xFFF59E0B)
private val UsdColor = Color(0xFF10B981)
private val StockColor = Color(0xFF6366F1)
private val CashColor = Color(0xFF8B5CF6)
private val CryptoColor = Color(0xFFF43F5E)

@Composable
fun PortfolioHomeScreen(
    viewModel: PortfolioViewModel,
    onExportRequested: () -> Unit = {},
    onImportRequested: () -> Unit = {},
    isPrivacyModeEnabled: Boolean = false,
    onTogglePrivacyMode: () -> Unit = {},
    onOpenCalculators: () -> Unit = {},
    onOpenBankAccounts: () -> Unit = {},
    onOpenDebtCredits: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenMutualFunds: () -> Unit = {},
    onOpenAiAnalysis: () -> Unit = {},
    onOpenOcrScanner: () -> Unit = {}
) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val totalRealizedPnl by viewModel.totalRealizedPnlRial.collectAsStateWithLifecycle()
    val totalDebt by viewModel.totalDebtRial.collectAsStateWithLifecycle()
    val totalCredit by viewModel.totalCreditRial.collectAsStateWithLifecycle()
    val sellError by viewModel.sellError.collectAsStateWithLifecycle()
    
    val totalValue = holdings.sumOf { it.currentValueRial }
    val totalPaid = holdings.sumOf { it.totalPaidRial }
    val totalPnl = totalValue - totalPaid
    val totalPnlPercent = if (totalPaid > 0) (totalPnl / totalPaid) * 100.0 else 0.0
    
    val totalDailyChangeRial = holdings.sumOf { it.dailyChangeRial }
    val totalDailyChangePercent = if (totalValue > 0) (totalDailyChangeRial / totalValue) * 100.0 else 0.0

    val netWorth = totalValue + totalCredit - totalDebt

    var menuExpanded by remember { mutableStateOf(false) }
    var sellTarget by remember { mutableStateOf<HoldingSummary?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(DarkSlateSurface)) {
        // --- Premium Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(CredifyIndigo.copy(alpha = 0.8f), DarkSlateSurface)
                    )
                )
                .padding(top = 24.dp, bottom = 40.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "خوش آمدید 👋",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Row {
                        IconButton(onClick = onTogglePrivacyMode) {
                            Icon(
                                if (isPrivacyModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = TextPrimary)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(DarkSlateSecondary)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("مدیریت بانک‌ها", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = CredifyIndigo) },
                                    onClick = { menuExpanded = false; onOpenBankAccounts() }
                                )
                                DropdownMenuItem(
                                    text = { Text("بده و بستان", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.History, null, tint = CredifyViolet) },
                                    onClick = { menuExpanded = false; onOpenDebtCredits() }
                                )
                                DropdownMenuItem(
                                    text = { Text("یادآورها", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Notifications, null, tint = GoldAccent) },
                                    onClick = { menuExpanded = false; onOpenReminders() }
                                )
                                DropdownMenuItem(
                                    text = { Text("هدف‌ها", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Flag, null, tint = EmeraldProfit) },
                                    onClick = { menuExpanded = false; onOpenGoals() }
                                )
                                DropdownMenuItem(
                                    text = { Text("صندوق‌های سرمایه‌گذاری", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.PieChart, null, tint = CredifySky) },
                                    onClick = { menuExpanded = false; onOpenMutualFunds() }
                                )
                                HorizontalDivider(color = SlateBorder)
                                DropdownMenuItem(
                                    text = { Text("تحلیل هوشمند (AI)", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = CredifyIndigo) },
                                    onClick = { menuExpanded = false; onOpenAiAnalysis() }
                                )
                                DropdownMenuItem(
                                    text = { Text("اسکن فاکتور", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.DocumentScanner, null, tint = CredifyViolet) },
                                    onClick = { menuExpanded = false; onOpenOcrScanner() }
                                )
                                HorizontalDivider(color = SlateBorder)
                                DropdownMenuItem(
                                    text = { Text("خروجی داده‌ها", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.CloudDownload, null, tint = TextSecondary) },
                                    onClick = { menuExpanded = false; onExportRequested() }
                                )
                                DropdownMenuItem(
                                    text = { Text("بازیابی داده‌ها", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.CloudUpload, null, tint = TextSecondary) },
                                    onClick = { menuExpanded = false; onImportRequested() }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("خالص دارایی (Net Worth)", color = TextSecondary, fontSize = 14.sp)
                com.example.ui.components.PrivacyAwareAmountText(
                    text = formatRial(netWorth),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (totalDailyChangeRial >= 0) EmeraldProfit else RoseLoss).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = formatPercentSigned(totalDailyChangePercent),
                            color = if (totalDailyChangeRial >= 0) EmeraldProfit else RoseLoss,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${if (totalDailyChangeRial >= 0) "+" else ""}${formatRial(totalDailyChangeRial)} امروز",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().offset(y = (-20).dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("ماشین‌حساب", Icons.Default.Calculate, CredifyIndigo, Modifier.weight(1f), onOpenCalculators)
                    QuickActionCard("پشتیبان‌گیری", Icons.Default.Backup, CredifyViolet, Modifier.weight(1f), onExportRequested)
                }
            }

            item {
                Text(
                    "ترکیب دارایی‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.5.dp, SlateBorderLight)
                ) {
                    PortfolioDonutChart(
                        slices = listOf(
                            DonutSlice("طلا", holdings.filter { it.assetType == PortfolioAssetType.GOLD }.sumOf { it.currentValueRial }, GoldColor),
                            DonutSlice("دلار", holdings.filter { it.assetType == PortfolioAssetType.USD }.sumOf { it.currentValueRial }, UsdColor),
                            DonutSlice("سهام", holdings.filter { it.assetType == PortfolioAssetType.STOCK }.sumOf { it.currentValueRial }, StockColor),
                            DonutSlice("نقد", holdings.filter { it.assetType == PortfolioAssetType.CASH }.sumOf { it.currentValueRial }, CashColor),
                            DonutSlice("کریپتو", holdings.filter { it.assetType == PortfolioAssetType.CRYPTO }.sumOf { it.currentValueRial }, CryptoColor)
                        ),
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "دارایی‌های من",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { /* View All logic */ }) {
                        Text("مشاهده همه", color = CredifyIndigo, fontSize = 12.sp)
                    }
                }
            }

            if (holdings.isEmpty()) {
                item {
                    EmptyHoldingsCard()
                }
            } else {
                items(holdings) { holding ->
                    HoldingCardPremium(holding, onSellClick = { sellTarget = holding })
                }
            }
        }
    }

    sellTarget?.let { holding ->
        SellAssetDialog(
            holding = holding,
            errorMessage = sellError,
            onDismiss = { sellTarget = null; viewModel.clearSellError() },
            onConfirm = { quantity, price ->
                viewModel.sellAsset(
                    assetType = holding.assetType,
                    assetCode = holding.assetCode,
                    assetName = holding.assetName,
                    quantitySold = quantity,
                    saleUnitPriceRial = price,
                    onSuccess = { sellTarget = null }
                )
            }
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HoldingCardPremium(holding: HoldingSummary, onSellClick: () -> Unit) {
    val icon = when (holding.assetType) {
        PortfolioAssetType.GOLD -> Icons.Default.BrightnessLow
        PortfolioAssetType.USD -> Icons.Default.MonetizationOn
        PortfolioAssetType.STOCK -> Icons.Default.Analytics
        PortfolioAssetType.CASH -> Icons.Default.AccountBalanceWallet
        PortfolioAssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
        PortfolioAssetType.FUND -> Icons.Default.PieChart
    }
    val iconColor = when (holding.assetType) {
        PortfolioAssetType.GOLD -> GoldColor
        PortfolioAssetType.USD -> UsdColor
        PortfolioAssetType.STOCK -> StockColor
        PortfolioAssetType.CASH -> CashColor
        PortfolioAssetType.CRYPTO -> CryptoColor
        PortfolioAssetType.FUND -> CredifySky
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).clickable { if (holding.assetType != PortfolioAssetType.CASH) onSellClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(holding.assetName, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "${com.example.util.PersianNumberUtils.formatDecimal(holding.quantity)} واحد",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                com.example.ui.components.PrivacyAwareAmountText(
                    text = formatRial(holding.currentValueRial),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = formatPercentSigned(holding.profitLossPercent),
                    color = if (holding.profitLossRial >= 0) EmeraldProfit else RoseLoss,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyHoldingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(48.dp), tint = TextMuted)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "هنوز دارایی ثبت نکرده‌اید",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "از منوی افزودن برای ثبت اولین خرید خود استفاده کنید.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
