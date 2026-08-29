package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.HoldingSummary
import com.example.ui.LocalIsRial
import com.example.domain.model.AllocationItem
import com.example.domain.model.GoldPriceAnalysis
import com.example.domain.model.PortfolioSummary
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianDateUtils
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import java.util.Locale

private val GoldColor = Color(0xFFF59E0B)
private val UsdColor = Color(0xFF10B981)
private val StockColor = Color(0xFF6366F1)
private val CashColor = Color(0xFF8B5CF6)
private val CryptoColor = Color(0xFFF43F5E)

enum class PortfolioUnit { TOMAN, USD, GOLD }

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
    onOpenOcrScanner: () -> Unit = {},
    onOpenAddPurchase: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val summary by viewModel.portfolioSummary.collectAsStateWithLifecycle()
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()
    val sellError by viewModel.sellError.collectAsStateWithLifecycle()
    
    var menuExpanded by remember { mutableStateOf(false) }
    var sellTarget by remember { mutableStateOf<HoldingSummary?>(null) }
    var selectedUnit by remember { mutableStateOf(PortfolioUnit.TOMAN) }

    Scaffold(
        containerColor = DarkSlateSurface
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Welcome & Menu Header ---
            item {
                HeaderToolbar(
                    isPrivacyModeEnabled = isPrivacyModeEnabled,
                    onTogglePrivacyMode = onTogglePrivacyMode,
                    onMenuClick = { menuExpanded = true },
                    onOpenBankAccounts = onOpenBankAccounts,
                    onOpenDebtCredits = onOpenDebtCredits,
                    onOpenReminders = onOpenReminders,
                    onOpenGoals = onOpenGoals,
                    onOpenMutualFunds = onOpenMutualFunds,
                    onOpenOcrScanner = onOpenOcrScanner,
                    onOpenSettings = onOpenSettings,
                    onExportRequested = onExportRequested,
                    onImportRequested = onImportRequested,
                    menuExpanded = menuExpanded,
                    onMenuDismiss = { menuExpanded = false }
                )
            }

            // --- Hero Dashboard Card ---
            item {
                summary?.let { 
                    PortfolioHeroCard(
                        summary = it,
                        selectedUnit = selectedUnit,
                        onUnitChange = { selectedUnit = it }
                    )
                } ?: SkeletonHeroCard()
            }

            // --- Quick Action Grid ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("افزودن دارایی", Icons.Default.Add, CryptoColor, Modifier.weight(1f), onOpenAddPurchase)
                    QuickActionCard("ماشین‌حساب", Icons.Default.Calculate, CredifyIndigo, Modifier.weight(1f), onOpenCalculators)
                    QuickActionCard("تحلیل هوشمند", Icons.Default.AutoAwesome, CredifyViolet, Modifier.weight(1f), onOpenAiAnalysis)
                }
            }

            // --- Insights Section ---
            item {
                summary?.let {
                    if (it.insights.isNotEmpty()) {
                        InsightsSection(it.insights)
                    }
                }
            }

            // --- Insights / Best & Worst ---
            item {
                summary?.let {
                    if (holdings.isNotEmpty()) {
                        PerformanceInsights(it)
                    }
                }
            }

            // --- Asset Allocation ---
            item {
                summary?.let {
                    if (holdings.isNotEmpty()) {
                        AllocationSection(it)
                    }
                }
            }

            // --- Gold Driver Analysis ---
            item {
                summary?.goldAnalysis?.let {
                    GoldAnalysisCard(it)
                }
            }

            // --- Performance Benchmark ---
            item {
                if (snapshots.isNotEmpty()) {
                    val points = snapshots.map {
                        BenchmarkPoint(
                            date = PersianDateUtils.formatJalaliDate(java.util.Date(it.timestamp)),
                            portfolioValue = it.totalValueRial,
                            goldValue = it.goldPriceRial,
                            usdValue = it.usdPriceRial
                        )
                    }
                    BenchmarkPerformanceChart(points = points, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }

            // --- Holdings List ---
            item {
                SectionHeader("دارایی‌های من", onActionClick = { /* View All */ })
            }

            if (holdings.isEmpty()) {
                item { EmptyHoldingsCard() }
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
fun HeaderToolbar(
    isPrivacyModeEnabled: Boolean,
    onTogglePrivacyMode: () -> Unit,
    onMenuClick: () -> Unit,
    onOpenBankAccounts: () -> Unit,
    onOpenDebtCredits: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenMutualFunds: () -> Unit,
    onOpenOcrScanner: () -> Unit,
    onOpenSettings: () -> Unit,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit,
    menuExpanded: Boolean,
    onMenuDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("نمای کلی سرمایه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row {
            IconButton(onClick = onTogglePrivacyMode) {
                Icon(if (isPrivacyModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
            }
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, null, tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = onMenuDismiss,
                    modifier = Modifier.background(DarkSlateSecondary)
                ) {
                    DropdownMenuItem(
                        text = { Text("مدیریت بانک‌ها", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = CredifyIndigo) },
                        onClick = { onMenuDismiss(); onOpenBankAccounts() }
                    )
                    DropdownMenuItem(
                        text = { Text("بده و بستان", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.History, null, tint = CredifyViolet) },
                        onClick = { onMenuDismiss(); onOpenDebtCredits() }
                    )
                    DropdownMenuItem(
                        text = { Text("یادآورها", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Notifications, null, tint = GoldAccent) },
                        onClick = { onMenuDismiss(); onOpenReminders() }
                    )
                    DropdownMenuItem(
                        text = { Text("هدف‌ها", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Flag, null, tint = EmeraldProfit) },
                        onClick = { onMenuDismiss(); onOpenGoals() }
                    )
                    DropdownMenuItem(
                        text = { Text("صندوق‌ها", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.PieChart, null, tint = CredifySky) },
                        onClick = { onMenuDismiss(); onOpenMutualFunds() }
                    )
                    HorizontalDivider(color = SlateBorder)
                    DropdownMenuItem(
                        text = { Text("اسکن فاکتور", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.DocumentScanner, null, tint = CredifyViolet) },
                        onClick = { onMenuDismiss(); onOpenOcrScanner() }
                    )
                    DropdownMenuItem(
                        text = { Text("پشتیبان‌گیری", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.CloudDownload, null, tint = TextSecondary) },
                        onClick = { onMenuDismiss(); onExportRequested() }
                    )
                    DropdownMenuItem(
                        text = { Text("بازیابی", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.CloudUpload, null, tint = TextSecondary) },
                        onClick = { onMenuDismiss(); onImportRequested() }
                    )
                    DropdownMenuItem(
                        text = { Text("تنظیمات", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Settings, null, tint = TextSecondary) },
                        onClick = { onMenuDismiss(); onOpenSettings() }
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioHeroCard(
    summary: PortfolioSummary,
    selectedUnit: PortfolioUnit,
    onUnitChange: (PortfolioUnit) -> Unit
) {
    val isRial = LocalIsRial.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if (isRial) "ارزش کل (ریال)" else "ارزش کل پورتفو", color = TextSecondary, fontSize = 13.sp)
                    val displayValue = when(selectedUnit) {
                        PortfolioUnit.TOMAN -> formatRial(summary.totalValueRial, isRial = isRial)
                        PortfolioUnit.USD -> "$${String.format(Locale.US, "%.2f", summary.totalValueRial / summary.usdRateRial)}"
                        PortfolioUnit.GOLD -> "${String.format(Locale.US, "%.3f", summary.totalValueRial / summary.gold18kPriceRial)} گرم طلا"
                    }
                    Text(displayValue, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                }
                UnitToggle(selectedUnit, onUnitChange)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("سود/زیان امروز", summary.todayProfitLossRial, summary.todayProfitLossPercent)
                VerticalDivider(modifier = Modifier.height(40.dp), color = SlateBorderLight)
                InfoItem("سود/زیان کل", summary.totalProfitLossRial, summary.totalProfitLossPercent)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = SlateBorderLight)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = TextMuted)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "آخرین بروزرسانی: ${PersianDateUtils.formatRelativeTime(summary.lastUpdated)}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(summary.marketStatus, fontSize = 11.sp, color = if (summary.todayProfitLossRial >= 0) EmeraldProfit else RoseLoss)
            }
        }
    }
}

@Composable
fun UnitToggle(selectedUnit: PortfolioUnit, onUnitChange: (PortfolioUnit) -> Unit) {
    val isRial = LocalIsRial.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSlateTertiary,
        modifier = Modifier.height(36.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            UnitButton(if (isRial) "ریال" else "تومان", selectedUnit == PortfolioUnit.TOMAN) { onUnitChange(PortfolioUnit.TOMAN) }
            UnitButton("USD", selectedUnit == PortfolioUnit.USD) { onUnitChange(PortfolioUnit.USD) }
            UnitButton("طلا", selectedUnit == PortfolioUnit.GOLD) { onUnitChange(PortfolioUnit.GOLD) }
        }
    }
}

@Composable
fun UnitButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CredifyIndigo else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (isSelected) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoItem(label: String, value: Double, percent: Double) {
    val isRial = LocalIsRial.current
    Column {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                formatRial(value, isRial = isRial),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (value >= 0) EmeraldProfit else RoseLoss
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                formatPercentSigned(percent),
                fontSize = 11.sp,
                color = if (percent >= 0) EmeraldProfit else RoseLoss
            )
        }
    }
}

@Composable
fun PerformanceInsights(summary: PortfolioSummary) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        summary.bestPerformer?.let { 
            PerformanceCard("🏆 بهترین امروز", it, EmeraldProfit, Modifier.weight(1f))
        }
        summary.worstPerformer?.let {
            PerformanceCard("📉 ضعیف‌ترین امروز", it, RoseLoss, Modifier.weight(1f))
        }
    }
}

@Composable
fun PerformanceCard(title: String, holding: HoldingSummary, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(holding.assetName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp, maxLines = 1)
            Text(formatPercentSigned(holding.dailyChangePercent), color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun AllocationSection(summary: PortfolioSummary) {
    var mode by remember { mutableIntStateOf(0) } // 0: By Asset, 1: By Type
    
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("ترکیب دارایی‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            TabRow(
                selectedTabIndex = mode,
                containerColor = Color.Transparent,
                contentColor = CredifyIndigo,
                divider = {},
                indicator = {},
                modifier = Modifier.width(160.dp).height(32.dp)
            ) {
                Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("دارایی", fontSize = 10.sp) })
                Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("نوع", fontSize = 10.sp) })
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(0.5.dp, SlateBorderLight)
        ) {
            val allocation = if (mode == 0) summary.allocationByAsset else summary.allocationByType
            PortfolioDonutChart(
                slices = allocation.mapIndexed { idx, it -> 
                    DonutSlice(it.label, it.valueRial, getAllocationColor(idx)) 
                },
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

fun getAllocationColor(index: Int): Color {
    val colors = listOf(CredifyIndigo, CredifyViolet, GoldColor, UsdColor, CryptoColor, CredifySky, Color(0xFFF97316), Color(0xFF6B7280))
    return colors[index % colors.size]
}

@Composable
fun SectionHeader(title: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        TextButton(onClick = onActionClick) {
            Text("مشاهده همه", color = CredifyIndigo, fontSize = 12.sp)
        }
    }
}

@Composable
fun SkeletonHeroCard() {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(20.dp).clip(RoundedCornerShape(32.dp)).background(DarkSlateSecondary))
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
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
            Text(title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HoldingCardPremium(holding: HoldingSummary, onSellClick: () -> Unit) {
    val isRial = LocalIsRial.current
    val icon = when (holding.assetType) {
        PortfolioAssetType.GOLD -> Icons.Default.BrightnessLow
        PortfolioAssetType.USD -> Icons.Default.MonetizationOn
        PortfolioAssetType.STOCK -> Icons.Default.Analytics
        PortfolioAssetType.CASH -> Icons.Default.AccountBalanceWallet
        PortfolioAssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
        PortfolioAssetType.FUND -> Icons.Default.PieChart
        PortfolioAssetType.REAL_ESTATE -> Icons.Default.Home
        PortfolioAssetType.VEHICLE -> Icons.Default.DirectionsCar
    }
    val iconColor = when (holding.assetType) {
        PortfolioAssetType.GOLD -> GoldColor
        PortfolioAssetType.USD -> UsdColor
        PortfolioAssetType.STOCK -> StockColor
        PortfolioAssetType.CASH -> CashColor
        PortfolioAssetType.CRYPTO -> CryptoColor
        PortfolioAssetType.FUND -> CredifySky
        PortfolioAssetType.REAL_ESTATE -> Color(0xFFF97316)
        PortfolioAssetType.VEHICLE -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).clickable { if (holding.assetType != PortfolioAssetType.CASH) onSellClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (holding.assetType == PortfolioAssetType.CRYPTO) {
                CryptoIcon(cmcId = holding.cmcId, symbol = holding.assetCode, size = 48.dp)
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
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
                Text(
                    text = formatRial(holding.currentValueRial, isRial = isRial),
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
fun InsightsSection(insights: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("مهم‌ترین اتفاقات پورتفو", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(0.5.dp, SlateBorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                insights.forEach { insight ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (insight.contains("⚠️")) RoseLoss else CredifySky))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(insight, fontSize = 11.sp, color = TextSecondary, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GoldAnalysisCard(analysis: GoldPriceAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("چرا قیمت طلا امروز تغییر کرد؟", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DriverItem("طلای جهانی", analysis.globalGoldChangePercent)
                DriverItem("دلار آمریکا", analysis.usdChangePercent)
                DriverItem("طلای ۱۸ عیار", analysis.localGoldChangePercent)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SlateBorderLight)
            Spacer(modifier = Modifier.height(12.dp))
            
            val message = if (analysis.primaryDriver == "GLOBAL_GOLD") {
                "رشد طلا بیشتر تحت تأثیر افزایش قیمت جهانی بوده است."
            } else {
                "بخش مهمی از تغییرات طلا ناشی از نوسانات دلار بوده است."
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(message, fontSize = 11.sp, color = TextSecondary, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun DriverItem(label: String, percent: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text(
            formatPercentSigned(percent),
            fontWeight = FontWeight.Bold,
            color = if (percent >= 0) EmeraldProfit else RoseLoss,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EmptyHoldingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
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
            Text("هنوز دارایی ثبت نکرده‌اید", color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("از منوی افزودن برای ثبت اولین خرید خود استفاده کنید.", color = TextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
