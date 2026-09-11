package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.AlertSetupDialog
import com.example.ui.components.CryptoIcon
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CryptoViewModel
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatUsd
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedWatchlistScreen(
    portfolioViewModel: PortfolioViewModel,
    cryptoViewModel: CryptoViewModel,
    onBack: () -> Unit,
    onAssetClick: (String, PortfolioAssetType) -> Unit
) {
    val categories by portfolioViewModel.watchlistCategories.collectAsStateWithLifecycle()
    val allCrypto by cryptoViewModel.allAssets.collectAsStateWithLifecycle()
    val allStocks by portfolioViewModel.watchlist.collectAsStateWithLifecycle()
    
    var selectedCategoryId by remember { mutableLongStateOf(0L) } // 0 means "All"
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    var alertAsset by remember { mutableStateOf<Triple<String, String, BigDecimal>?>(null) } // Code, Name, CurrentPriceRial

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("دیده‌بان پیشرفته", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) { Icon(Icons.Default.CreateNewFolder, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("جستجوی نماد یا نام ارز...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Categories Tabs
            ScrollableTabRow(
                selectedTabIndex = if (selectedCategoryId == 0L) 0 else categories.indexOfFirst { it.id == selectedCategoryId } + 1,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                divider = {}
            ) {
                Tab(
                    selected = selectedCategoryId == 0L,
                    onClick = { selectedCategoryId = 0L },
                    text = { Text("همه", modifier = Modifier.padding(vertical = 8.dp)) }
                )
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        text = { Text(category.name, modifier = Modifier.padding(vertical = 8.dp)) }
                    )
                }
            }

            // Asset List
            val filteredCrypto = allCrypto.filter { 
                (searchQuery.isEmpty() || it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true)) &&
                (selectedCategoryId == 0L || it.isInWatchlist) // Simple logic for now, M2M needs more wiring
            }
            
            val filteredStocks = allStocks.filter {
                (searchQuery.isEmpty() || it.symbol.contains(searchQuery, true) || it.fullName.contains(searchQuery, true))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredStocks.isNotEmpty()) {
                    item { Text("بورس تهران", style = DaraTypography.labelSmall, color = Slate400) }
                    items(filteredStocks) { stock ->
                        WatchlistListItem(
                            name = stock.fullName,
                            symbol = stock.symbol,
                            price = com.example.util.formatRial(stock.lastPriceRial, isRial = true),
                            change = stock.changePercent,
                            onAlertClick = { alertAsset = Triple(stock.symbol, stock.fullName, stock.lastPriceRial) },
                            onClick = { onAssetClick(stock.symbol, PortfolioAssetType.STOCK) }
                        )
                    }
                }

                if (filteredCrypto.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item { Text("بازار جهانی (کریپتو)", style = DaraTypography.labelSmall, color = Slate400) }
                    items(filteredCrypto) { crypto ->
                        WatchlistListItem(
                            name = crypto.name,
                            symbol = crypto.symbol,
                            price = formatUsd(crypto.priceUsd ?: BigDecimal.ZERO),
                            change = crypto.percentChange24h ?: BigDecimal.ZERO,
                            isCrypto = true,
                            cmcId = crypto.cmcId,
                            onAlertClick = { 
                                val usdRate = portfolioViewModel.usdPriceToman.value.multiply(BigDecimal("10"))
                                val priceRial = (crypto.priceUsd ?: BigDecimal.ZERO).multiply(usdRate)
                                alertAsset = Triple(crypto.symbol, crypto.name, priceRial) 
                            },
                            onClick = { onAssetClick(crypto.symbol, PortfolioAssetType.CRYPTO) }
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                portfolioViewModel.addWatchlistCategory(name)
                showAddCategoryDialog = false
            }
        )
    }

    alertAsset?.let { (code, name, price) ->
        AlertSetupDialog(
            assetName = name,
            assetCode = code,
            currentPriceRial = price,
            onDismiss = { alertAsset = null },
            onConfirm = { alert ->
                portfolioViewModel.addAlert(alert)
                alertAsset = null
            }
        )
    }
}

@Composable
fun WatchlistListItem(
    name: String,
    symbol: String,
    price: String,
    change: BigDecimal,
    isCrypto: Boolean = false,
    cmcId: Int? = null,
    onAlertClick: () -> Unit,
    onClick: () -> Unit
) {
    val changeColor = if (change.signum() >= 0) EmeraldCore else RoseCoral

    DaraGlassCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCrypto && cmcId != null) {
                CryptoIcon(cmcId = cmcId, symbol = symbol, size = 36.dp)
            } else {
                Surface(
                    shape = CircleShape,
                    color = IndigoElectric.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShowChart, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = DaraTypography.titleSmall, fontWeight = FontWeight.Bold, color = Slate50, maxLines = 1)
                Text(symbol, style = DaraTypography.labelSmall, color = Slate400)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(price, style = DaraTypography.bodyLarge, fontWeight = FontWeight.Bold, color = Slate50)
                Text(
                    text = formatPercentSigned(change),
                    style = DaraTypography.labelSmall,
                    color = changeColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onAlertClick) {
                Icon(Icons.Default.NotificationsNone, null, tint = Slate600, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ایجاد دسته‌بندی جدید") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام دسته (مثلاً DeFi)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("تأیید") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
