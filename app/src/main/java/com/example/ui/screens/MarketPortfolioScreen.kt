package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MarketRateEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.AssetGrowthChart
import com.example.ui.components.BottomTab
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.CredifyHeaderCard
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.LoanCalculatorCard
import com.example.ui.components.NavFundsSection
import com.example.ui.components.OfflineBanner
import com.example.ui.components.TransactionActionDialog
import com.example.ui.components.QuickActionType
import com.example.ui.components.QuickActionsGrid
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.MarketPortfolioViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPortfolioScreen(
    viewModel: MarketPortfolioViewModel,
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToDeposit: () -> Unit = {},
    onNavigateToTransfer: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToCalculators: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val balance by viewModel.portfolioBalance.collectAsStateWithLifecycle()
    val pnl by viewModel.pnlPercentage.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val rates by viewModel.marketRates.collectAsStateWithLifecycle()
    val funds by viewModel.mutualFunds.collectAsStateWithLifecycle()
    val txList by viewModel.transactions.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    var activeDialogAction by remember { mutableStateOf<QuickActionType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CredifyIndigo.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CredifyIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "دفتر محاسبات مالی",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "سیستم مدیریت دارایی و بازار",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadData() },
                        modifier = Modifier.testTag("button_refresh_market")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Market Rates",
                            tint = CredifyIndigo
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSecurity,
                        modifier = Modifier.testTag("button_security_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security Settings",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            FloatingBottomBar(
                currentTab = selectedTab,
                onTabSelected = { tab ->
                    viewModel.selectTab(tab)
                    when (tab) {
                        BottomTab.PORTFOLIO -> { /* stay on home */ }
                        BottomTab.TRANSACTIONS -> onNavigateToAnalytics()
                        // FIX: this used to incorrectly call onNavigateToSwap(); the
                        // Calculator tab now correctly opens the Calculators hub.
                        BottomTab.CALCULATOR -> onNavigateToCalculators()
                        BottomTab.SECURITY -> onNavigateToSecurity()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("market_portfolio_lazy_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline Banner Fallback
            if (isOffline) {
                item {
                    OfflineBanner()
                }
            }

            // Credify Header Card
            item {
                CredifyHeaderCard(
                    portfolioBalanceToman = balance,
                    pnlPercentage = pnl
                )
            }

            // Quick Actions Grid (Deposit, Transfer, Swap, Analytics)
            item {
                QuickActionsGrid(
                    onActionSelected = { action ->
                        when (action) {
                            QuickActionType.DEPOSIT -> onNavigateToDeposit()
                            QuickActionType.TRANSFER -> onNavigateToTransfer()
                            QuickActionType.SWAP -> onNavigateToSwap()
                            QuickActionType.ANALYTICS -> onNavigateToAnalytics()
                        }
                    }
                )
            }

            // Live Gold & Currency Rates Horizontal Overview
            item {
                LiveMarketOverviewSection(rates = rates)
            }

            // NavFundsSection
            item {
                NavFundsSection(
                    funds = funds
                )
            }

            // High-Performance Interactive Asset Growth Chart
            item {
                AssetGrowthChart(
                    dataPoints = listOf(
                        ChartDataPoint("۱۴۰۳/۰۱/۰۱", 1200000000f),
                        ChartDataPoint("۱۴۰۳/۰۲/۰۱", 1450000000f),
                        ChartDataPoint("۱۴۰۳/۰۳/۰۱", 1380000000f),
                        ChartDataPoint("۱۴۰۳/۰۴/۰۱", 1850000000f),
                        ChartDataPoint("۱۴۰۳/۰۵/۰۱", 2100000000f),
                        ChartDataPoint("۱۴۰۳/۰۵/۱۵", balance.toFloat())
                    )
                )
            }

            // Loan Amortization Calculator
            item {
                LoanCalculatorCard()
            }

            // Recent Ledger Transactions
            item {
                Text(
                    text = "دفتر تراکنش‌های اخیر",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (txList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز تراکنشی ثبت نشده است. از دکمه‌های بالا برای واریز یا انتقال استفاده کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                items(txList, key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onDelete = { viewModel.deleteTransaction(tx.id) }
                    )
                }
            }
        }
    }

    // Fallback Dialog for Quick Actions
    activeDialogAction?.let { action ->
        TransactionActionDialog(
            actionType = action,
            onDismiss = { activeDialogAction = null },
            onConfirmTransaction = { title, amount, type, category ->
                viewModel.addTransaction(title, amount, type, category)
            }
        )
    }
}

@Composable
private fun LiveMarketOverviewSection(rates: List<MarketRateEntity>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "نرخ‌های زنده طلا و ارز",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rates.forEach { rate ->
                MarketRateSmallCard(
                    rate = rate,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MarketRateSmallCard(
    rate: MarketRateEntity,
    modifier: Modifier = Modifier
) {
    val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(rate.priceToman.toLong())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("market_card_${rate.assetCode}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = rate.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedPrice,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (rate.changePercent >= 0) "+${rate.changePercent}%" else "${rate.changePercent}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = if (rate.changePercent >= 0) EmeraldProfit else RoseLoss,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(transaction.amount.toLong())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.DEPOSIT -> EmeraldProfit.copy(alpha = 0.15f)
                                TransactionType.TRANSFER -> CredifyIndigo.copy(alpha = 0.15f)
                                TransactionType.SWAP -> GoldAccent.copy(alpha = 0.15f)
                                TransactionType.EXPENSE -> RoseLoss.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = when (transaction.type) {
                            TransactionType.DEPOSIT -> EmeraldProfit
                            TransactionType.TRANSFER -> CredifyIndigo
                            TransactionType.SWAP -> GoldAccent
                            TransactionType.EXPENSE -> RoseLoss
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.DEPOSIT -> "+$formattedAmount تومان"
                        else -> "-$formattedAmount تومان"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = when (transaction.type) {
                        TransactionType.DEPOSIT -> EmeraldProfit
                        else -> RoseLoss
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_tx_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
