package com.example.ui.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.PortfolioAssetType
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.DashboardSkeleton
import com.example.ui.components.DonutSlice
import com.example.ui.components.PortfolioDonutChart
import com.example.ui.theme.*
import com.example.util.PersianNumberUtils
import com.example.util.formatPercentSigned
import com.example.util.formatRial

@Composable
fun DaraDashboardScreen(
    viewModel: com.example.ui.viewmodel.PortfolioViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToHealth: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    val totalValueRial by viewModel.totalPortfolioValueRial.collectAsStateWithLifecycle()
    val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    
    var isBalanceVisible by remember { mutableStateOf(true) }

    if (holdings.isEmpty() && isRefreshing) {
        DashboardSkeleton()
        return
    }

    Scaffold(
        topBar = { 
            DaraTopHeader(
                onSearchClick = onNavigateToSearch,
                onAlertsClick = onNavigateToAlerts
            ) 
        },
        containerColor = ObsidianSlate900
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome & User Info
            DaraWelcomeSection(
                isBalanceVisible = isBalanceVisible,
                onToggleBalance = { isBalanceVisible = !isBalanceVisible }
            )

            // Hero Wealth Card
            DaraWealthCard(
                totalValueRial = totalValueRial,
                isBalanceVisible = isBalanceVisible,
                onRiskAnalysisClick = onNavigateToHealth
            )

            // Live Market Ticker
            DaraMarketTicker(
                marketRates = marketRates,
                onSeeMore = onNavigateToMarket
            )

            // Asset Allocation
            DaraAssetAllocation(viewModel = viewModel)

            // AI Insights / Editorial
            DaraEditorialHighlight(onNavigateToScanner = onNavigateToScanner)

            // Recent Transactions Summary
            DaraRecentTransactions(viewModel = viewModel)

            // Security Assurance
            DaraSecurityRibbon()
        }
    }
}

@Composable
fun DaraTopHeader(
    onSearchClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida/AEtjO1WKb-JdsP2NuvqL2iG_BxlPdgxrOhrOUd38Uq79YflzYtfw0btA5u1Leayr4ywNjITQ0m4tqK6H_6JcBKS_ctY9J9Mexqqim6vyQb1ktoMpFvZ9IB7ID0fbHW8B-gTkwM7ffip97krcMQFlUfqJAw6PTpe9RqefbKcdV4VcCKyRc24z_m8a81BghgI9zL__-G4zB8gsE0CVFM8OZdwyjQgSv-wLplJHAF-SgdtyiYDfAMeCs8jIQ9G3iDc",
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("دارا", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(percent = 100),
                            color = Color(0xFF00A572).copy(alpha = 0.2f)
                        ) {
                            Text(
                                "ویژه",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = DaraTypography.labelSmall,
                                color = Color(0xFF4EDE93)
                            )
                        }
                    }
                    Text("Dashboard", style = DaraTypography.labelSmall, color = Slate400)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, null, tint = Slate400)
                }
                IconButton(onClick = onAlertsClick) {
                    Icon(Icons.Default.Notifications, null, tint = Slate400)
                }
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDlWqJtR7qE6yDwb5YoQy6JciiWLwRIPDT7p3HMc83WcubH55WAejDHs2EixhCX9lMv1VtwEtoxLpr7QGocN2w5Py8v6TYedBZDjMbP5sLn0inJz-xmA7_kBGdr9mnLe-Sclxf1S2xo9raBdwfcUcY1OxLZ95NC_5PyNUTULQ9Gq1igjkgkSPSEg6rnihzPbPhp9eBYsu9JqigB6hXdgQZ_liBPaPrv_Od1GQDdZuaGwJLoXM2Fs6YJ",
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}



@Composable
fun DaraWelcomeSection(
    isBalanceVisible: Boolean,
    onToggleBalance: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "روز بخیر، سهراب عزیز",
                    style = DaraTypography.headlineMedium,
                    color = Slate50,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = RefinedAmberGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 100),
                    color = ObsidianSlate600,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = IndigoElectric,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سطح الماس دارا",
                            style = DaraTypography.labelSmall,
                            color = Slate400
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(EmeraldCore, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بروزرسانی زنده",
                        style = DaraTypography.labelSmall,
                        color = EmeraldCore
                    )
                }
            }
        }

        IconButton(
            onClick = onToggleBalance,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSlate600)
        ) {
            Icon(
                imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DaraWealthCard(
    totalValueRial: Double,
    isBalanceVisible: Boolean,
    onRiskAnalysisClick: () -> Unit = {}
) {
    val isRial = LocalIsRial.current
    DaraGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(IndigoElectric, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ارزش کل دارایی‌ها",
                        style = DaraTypography.labelMedium,
                        color = Slate400
                    )
                }
                Surface(
                    onClick = onRiskAnalysisClick,
                    shape = RoundedCornerShape(percent = 100),
                    color = ObsidianSlate500.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "تحلیل ریسک",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = DaraTypography.labelSmall,
                        color = IndigoElectric,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (isBalanceVisible) formatRial(totalValueRial, showSuffix = false, isRial = isRial) else "••••••••••••",
                    style = DaraTypography.displayLarge,
                    color = Slate50,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRial) "ریال" else "تومان",
                    style = DaraTypography.titleMedium,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 100),
                    color = EmeraldCore.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = EmeraldCore,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+۳.۸٪",
                            style = DaraTypography.labelMedium,
                            color = EmeraldCore,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "عملکرد امروز",
                    style = DaraTypography.labelSmall,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WealthActionButton(icon = Icons.Default.AddCard, label = "واریز شتابی", color = IndigoElectric)
                WealthActionButton(icon = Icons.Default.Output, label = "برداشت آنی", color = Slate400)
                WealthActionButton(icon = Icons.Default.MonetizationOn, label = "خرید طلا", color = RefinedAmberGold)
                WealthActionButton(icon = Icons.Default.SwapHoriz, label = "تبدیل", color = EmeraldCore)
            }
        }
    }
}

@Composable
fun WealthActionButton(icon: ImageVector, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            onClick = { /* TODO */ }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = DaraTypography.labelSmall,
            color = Slate50,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DaraMarketTicker(
    marketRates: List<com.example.data.local.MarketRateEntity>,
    onSeeMore: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = IndigoElectric, modifier = Modifier.size(18.dp))
                Text("قیمت‌های زنده بازار", style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onSeeMore() }
            ) {
                Text("نرخ مرجع", style = DaraTypography.labelSmall, color = Slate400)
                Icon(Icons.Default.Schedule, null, tint = Slate400, modifier = Modifier.size(14.dp))
            }
        }

        LazyRow(
            contentPadding = PaddingValues(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(marketRates.take(5)) { rate ->
                DaraTickerItem(rate)
            }
        }
    }
}

@Composable
fun DaraTickerItem(rate: com.example.data.local.MarketRateEntity) {
    val isProfit = rate.changePercent >= 0
    val accentColor = if (isProfit) EmeraldCore else RoseCoral
    
    DaraGlassCard(
        modifier = Modifier.width(160.dp),
        cornerRadius = 12.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(rate.name, style = DaraTypography.labelMedium, color = Slate50, maxLines = 1)
                Text(
                    text = formatPercentSigned(rate.changePercent),
                    style = DaraTypography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = PersianNumberUtils.toPersianDigits(rate.priceToman.toLong().toString()),
                    style = DaraTypography.titleMedium,
                    color = Slate50,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("ت", style = DaraTypography.labelSmall, color = Slate400)
            }
        }
    }
}

@Composable
fun DaraAssetAllocation(viewModel: com.example.ui.viewmodel.PortfolioViewModel) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    
    val slices = holdings.groupBy { it.assetType }.map { (type, assets) ->
        val totalValue = assets.sumOf { it.currentValueRial }
        val label = when(type) {
            PortfolioAssetType.GOLD -> "طلا"
            PortfolioAssetType.STOCK -> "بورس"
            PortfolioAssetType.CRYPTO -> "کریپتو"
            PortfolioAssetType.CASH -> "نقدینگی"
            else -> "سایر"
        }
        val color = when(type) {
            PortfolioAssetType.GOLD -> RefinedAmberGold
            PortfolioAssetType.STOCK -> IndigoElectric
            PortfolioAssetType.CRYPTO -> EmeraldCore
            PortfolioAssetType.CASH -> IndigoGlow
            else -> Slate400
        }
        DonutSlice(label, totalValue, color)
    }

    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DonutLarge, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                    Text("ترکیب سبد دارایی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                }
                Text("تحلیل ریسک", style = DaraTypography.labelSmall, color = IndigoElectric, modifier = Modifier.clickable { })
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (slices.isNotEmpty()) {
                PortfolioDonutChart(slices = slices)
            } else {
                Text("هنوز دارایی‌ای ثبت نشده", style = DaraTypography.bodySmall, color = Slate400)
            }
        }
    }
}

@Composable
fun DaraEditorialHighlight(onNavigateToScanner: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToScanner() },
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate600.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSlate500)
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = RefinedAmberGold, modifier = Modifier.align(Alignment.Center))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("مشاوره اختصاصی دارا", style = DaraTypography.labelSmall, color = RefinedAmberGold, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Bolt, null, tint = RefinedAmberGold, modifier = Modifier.size(12.dp))
                }
                Text(
                    text = "فرصت گواهی شمش طلای بورس",
                    style = DaraTypography.titleMedium,
                    color = Slate50,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "حفاظت در برابر نوسانات ارزی همراه با بازدهی تضمین‌شده",
                    style = DaraTypography.bodySmall,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(Icons.Default.ChevronLeft, null, tint = Slate50)
        }
    }
}

@Composable
fun DaraRecentTransactions(viewModel: com.example.ui.viewmodel.PortfolioViewModel) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.HistoryEdu, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                Text("تراکنش‌های اخیر", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Text("مشاهده همه", style = DaraTypography.labelSmall, color = IndigoElectric, modifier = Modifier.clickable { })
        }

        purchases.take(3).forEach { purchase ->
            DaraTransactionItem(purchase)
        }
    }
}

@Composable
fun DaraTransactionItem(purchase: com.example.data.local.AssetPurchaseEntity) {
    val isRial = LocalIsRial.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSlate700)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = IndigoElectric.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = IndigoElectric,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(purchase.assetName, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.SemiBold)
                Text(
                    text = purchase.assetCode,
                    style = DaraTypography.bodySmall,
                    color = Slate400
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatRial(purchase.totalPaidRial, showSuffix = false, isRial = isRial),
                style = DaraTypography.titleSmall,
                color = Slate50,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isRial) "ریال" else "تومان",
                style = DaraTypography.labelSmall,
                color = Slate400
            )
        }
    }
}

@Composable
fun DaraSecurityRibbon() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSlate800
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, null, tint = EmeraldCore, modifier = Modifier.size(18.dp))
                Text(
                    "نگهداری امن در صندوق‌های امانات بانکی",
                    style = DaraTypography.labelSmall,
                    color = Slate400
                )
            }
            Text("اطلاعیه امنیتی", style = DaraTypography.labelSmall, color = IndigoElectric, fontWeight = FontWeight.Bold)
        }
    }
}
