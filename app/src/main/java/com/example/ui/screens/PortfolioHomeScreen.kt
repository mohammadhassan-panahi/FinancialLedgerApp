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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.ui.LocalIsRial
import com.example.domain.model.GoldPriceAnalysis
import com.example.domain.model.Holding
import com.example.domain.model.PortfolioSummary
import com.example.ui.UiState
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianDateUtils
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import com.example.util.safeDiv
import java.math.BigDecimal
import java.util.Locale

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
    val holdingsState by viewModel.holdingsState.collectAsStateWithLifecycle()
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()
    val sellError by viewModel.sellError.collectAsStateWithLifecycle()
    
    var menuExpanded by remember { mutableStateOf(false) }
    var sellTarget by remember { mutableStateOf<Holding?>(null) }
    var selectedUnit by remember { mutableStateOf(PortfolioUnit.TOMAN) }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            HeaderToolbar(
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- Hero Dashboard Card ---
            item {
                when (val state = summaryState) {
                    is UiState.Success -> {
                        state.data?.let { 
                            PortfolioHeroCard(
                                summary = it,
                                selectedUnit = selectedUnit,
                                onUnitChange = { unit -> selectedUnit = unit }
                            )
                        } ?: SkeletonHeroCard()
                    }
                    is UiState.Loading -> SkeletonHeroCard()
                    else -> SkeletonHeroCard()
                }
            }

            // --- Quick Action Grid ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("افزودن دارایی", Icons.Default.Add, RoseCoral, Modifier.weight(1f), onOpenAddPurchase)
                    QuickActionCard("ماشین‌حساب", Icons.Default.Calculate, IndigoElectric, Modifier.weight(1f), onOpenCalculators)
                    QuickActionCard("تحلیل هوشمند", Icons.Default.AutoAwesome, Color(0xFF8B5CF6), Modifier.weight(1f), onOpenAiAnalysis)
                }
            }

            // --- Insights Section ---
            item {
                if (summaryState is UiState.Success) {
                    val summary = (summaryState as UiState.Success).data
                    summary?.let {
                        if (it.insights.isNotEmpty()) {
                            InsightsSection(it.insights)
                        }
                    }
                }
            }

            // --- Performance Benchmark ---
            item {
                if (snapshots.isNotEmpty()) {
                    val points = snapshots.map {
                        BenchmarkPoint(
                            date = PersianDateUtils.formatJalaliDate(java.util.Date(it.timestamp)),
                            portfolioValue = it.totalValueRial.toDouble(),
                            goldValue = it.goldPriceRial.toDouble(),
                            usdValue = it.usdPriceRial.toDouble()
                        )
                    }
                    DaraGlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BenchmarkPerformanceChart(points = points, modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // --- Holdings List ---
            item {
                SectionHeader("دارایی‌های من", onActionClick = { /* View All */ })
            }

            when (val state = holdingsState) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item { EmptyHoldingsCard() }
                    } else {
                        items(state.data) { holding ->
                            HoldingCardPremium(holding, onSellClick = { sellTarget = holding })
                        }
                    }
                }
                is UiState.Loading -> {
                    items(3) { Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).background(ObsidianSlate800)) }
                }
                else -> { item { Text("Error loading holdings", color = RoseCoral, modifier = Modifier.padding(16.dp)) } }
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
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("نمای کلی سرمایه", style = DaraTypography.titleLarge, fontWeight = FontWeight.Bold, color = Slate50)
        Row {
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, null, tint = Slate400)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = onMenuDismiss,
                    modifier = Modifier.background(ObsidianSlate800)
                ) {
                    DropdownMenuItem(
                        text = { Text("مدیریت بانک‌ها", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = IndigoElectric) },
                        onClick = { onMenuDismiss(); onOpenBankAccounts() }
                    )
                    DropdownMenuItem(
                        text = { Text("بده و بستان", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.History, null, tint = Color(0xFF8B5CF6)) },
                        onClick = { onMenuDismiss(); onOpenDebtCredits() }
                    )
                    DropdownMenuItem(
                        text = { Text("یادآورها", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.Notifications, null, tint = RefinedAmberGold) },
                        onClick = { onMenuDismiss(); onOpenReminders() }
                    )
                    DropdownMenuItem(
                        text = { Text("هدف‌ها", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.Flag, null, tint = EmeraldCore) },
                        onClick = { onMenuDismiss(); onOpenGoals() }
                    )
                    DropdownMenuItem(
                        text = { Text("صندوق‌ها", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.PieChart, null, tint = Color(0xFF0EA5E9)) },
                        onClick = { onMenuDismiss(); onOpenMutualFunds() }
                    )
                    HorizontalDivider(color = ObsidianSlate700)
                    DropdownMenuItem(
                        text = { Text("اسکن فاکتور", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.DocumentScanner, null, tint = Color(0xFF8B5CF6)) },
                        onClick = { onMenuDismiss(); onOpenOcrScanner() }
                    )
                    DropdownMenuItem(
                        text = { Text("پشتیبان‌گیری", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.CloudDownload, null, tint = Slate400) },
                        onClick = { onMenuDismiss(); onExportRequested() }
                    )
                    DropdownMenuItem(
                        text = { Text("بازیابی", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.CloudUpload, null, tint = Slate400) },
                        onClick = { onMenuDismiss(); onImportRequested() }
                    )
                    DropdownMenuItem(
                        text = { Text("تنظیمات", color = Slate50) },
                        leadingIcon = { Icon(Icons.Default.Settings, null, tint = Slate400) },
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
    DaraGlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if (isRial) "ارزش کل (ریال)" else "ارزش کل پورتفو", color = Slate400, fontSize = 12.sp)
                    val displayValue = when(selectedUnit) {
                        PortfolioUnit.TOMAN -> formatRial(summary.totalValueRial, isRial = isRial)
                        PortfolioUnit.USD -> "$${String.format(Locale.US, "%,.2f", (summary.totalValueRial.safeDiv(summary.usdRateRial)).toDouble())}"
                        PortfolioUnit.GOLD -> "${String.format(Locale.US, "%,.3f", (summary.totalValueRial.safeDiv(summary.gold18kPriceRial)).toDouble())} گرم طلا"
                    }
                    Text(displayValue, style = DaraTypography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Slate50)
                }
                UnitToggle(selectedUnit, onUnitChange)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("سود امروز", summary.todayProfitLossRial, summary.todayProfitLossPercent)
                InfoItem("سود کل", summary.totalProfitLossRial, summary.totalProfitLossPercent)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GlassBorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(12.dp), tint = Slate600)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "آخرین بروزرسانی: ${PersianDateUtils.formatRelativeTime(summary.lastUpdated)}",
                    style = DaraTypography.labelSmall,
                    color = Slate600
                )
            }
        }
    }
}

@Composable
fun UnitToggle(selectedUnit: PortfolioUnit, onUnitChange: (PortfolioUnit) -> Unit) {
    val isRial = LocalIsRial.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSlate600.copy(alpha = 0.5f),
        modifier = Modifier.height(32.dp)
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
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
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) IndigoElectric else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (isSelected) Color.White else Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoItem(label: String, value: BigDecimal, percent: BigDecimal) {
    val isRial = LocalIsRial.current
    Column {
        Text(label, color = Slate400, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                formatRial(value, isRial = isRial),
                style = DaraTypography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (value.compareTo(BigDecimal.ZERO) >= 0) EmeraldCore else RoseCoral
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                formatPercentSigned(percent),
                style = DaraTypography.labelSmall,
                color = if (percent.compareTo(BigDecimal.ZERO) >= 0) EmeraldCore else RoseCoral
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = DaraTypography.titleMedium, fontWeight = FontWeight.Bold, color = Slate50)
        TextButton(onClick = onActionClick) {
            Text("مشاهده همه", color = IndigoElectric, style = DaraTypography.labelMedium)
        }
    }
}

@Composable
fun SkeletonHeroCard() {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(16.dp).clip(RoundedCornerShape(24.dp)).background(ObsidianSlate800))
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    DaraGlassCard(
        modifier = modifier.height(90.dp).clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
            Text(title, color = Slate50, style = DaraTypography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HoldingCardPremium(holding: Holding, onSellClick: () -> Unit) {
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
        PortfolioAssetType.GOLD -> RefinedAmberGold
        PortfolioAssetType.USD -> EmeraldCore
        PortfolioAssetType.STOCK -> IndigoElectric
        PortfolioAssetType.CASH -> Color(0xFF8B5CF6)
        PortfolioAssetType.CRYPTO -> RoseCoral
        PortfolioAssetType.FUND -> Color(0xFF0EA5E9)
        PortfolioAssetType.REAL_ESTATE -> Color(0xFFF97316)
        PortfolioAssetType.VEHICLE -> Color(0xFF6B7280)
    }

    DaraGlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).clickable { if (holding.assetType != PortfolioAssetType.CASH) onSellClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (holding.assetType == PortfolioAssetType.CRYPTO) {
                CryptoIcon(cmcId = holding.cmcId, symbol = holding.assetCode, size = 44.dp)
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(holding.assetName, style = DaraTypography.titleSmall, fontWeight = FontWeight.Bold, color = Slate50)
                Text(
                    "${com.example.util.PersianNumberUtils.formatDecimal(holding.quantity)} واحد",
                    color = Slate400,
                    style = DaraTypography.labelSmall
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRial(holding.currentValueRial, isRial = isRial),
                    style = DaraTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate50
                )
                Text(
                    text = formatPercentSigned(holding.profitLossPercent),
                    color = if (holding.profitLossRial.compareTo(BigDecimal.ZERO) >= 0) EmeraldCore else RoseCoral,
                    style = DaraTypography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InsightsSection(insights: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("اتفاقات پورتفو", style = DaraTypography.titleSmall, fontWeight = FontWeight.Bold, color = Slate50)
        Spacer(modifier = Modifier.height(12.dp))
        
        DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                insights.take(3).forEach { insight ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (insight.contains("⚠️")) RoseCoral else Color(0xFF0EA5E9)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(insight, style = DaraTypography.labelMedium, color = Slate400, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHoldingsCard() {
    DaraGlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(48.dp), tint = Slate600)
            Spacer(modifier = Modifier.height(16.dp))
            Text("هنوز دارایی ثبت نکرده‌اید", color = Slate50, fontWeight = FontWeight.Bold)
            Text("برای شروع اولین خرید خود را ثبت کنید.", color = Slate400, style = DaraTypography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}
