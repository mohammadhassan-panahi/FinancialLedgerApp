package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetFormScreen(
    viewModel: PortfolioViewModel,
    assetType: PortfolioAssetType,
    prefilledName: String? = null,
    onBack: () -> Unit,
    onSubmit: (String, BigDecimal, BigDecimal, Long) -> Unit
) {
    var assetName by remember { mutableStateOf(prefilledName ?: "") }
    var quantity by remember { mutableStateOf("۱") }
    var unitPriceRial by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val purchaseDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    val allMarketRates by viewModel.marketRates.collectAsStateWithLifecycle()
    val cryptoAssets by viewModel.cryptoAssets.collectAsStateWithLifecycle()
    val mutualFunds by viewModel.mutualFunds.collectAsStateWithLifecycle()

    val usdRateToman = allMarketRates.find { it.assetCode == "USD" }?.priceToman ?: BigDecimal("65000")
    val rialPerToman = BigDecimal("10")

    fun autoFillPrice() {
        val livePriceRial = when (assetType) {
            PortfolioAssetType.GOLD -> {
                val rate = allMarketRates.find { it.assetCode.contains("GOLD_18K") || it.name.contains("۱۸") }
                rate?.priceToman?.multiply(rialPerToman)
            }
            PortfolioAssetType.USD -> usdRateToman.multiply(rialPerToman)
            PortfolioAssetType.CRYPTO -> {
                // If user entered a symbol, try to find it
                val crypto = cryptoAssets.find { it.symbol.equals(assetName.trim(), ignoreCase = true) }
                crypto?.priceUsd?.multiply(usdRateToman)?.multiply(rialPerToman)
            }
            PortfolioAssetType.FUND -> {
                val fund = mutualFunds.find { it.name.contains(assetName.trim()) || it.id.equals(assetName.trim(), ignoreCase = true) }
                fund?.navToman?.multiply(rialPerToman)
            }
            else -> null
        }
        if (livePriceRial != null) {
            unitPriceRial = livePriceRial.toPlainString()
        }
    }

    LaunchedEffect(prefilledName) {
        if (!prefilledName.isNullOrBlank()) {
            autoFillPrice()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("تأیید") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("انصراف") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val q = PersianNumberUtils.parseAmount(quantity)
    val p = PersianNumberUtils.parseAmount(unitPriceRial)
    val isValid = assetName.isNotBlank() && q > BigDecimal.ZERO && p > BigDecimal.ZERO

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            AddAssetHeader(onBack = onBack)
        },
        bottomBar = {
            SubmitAssetFooter(
                total = q.multiply(p),
                enabled = isValid,
                onClick = { onSubmit(assetName, q, p, purchaseDate) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProgressInfoCard(assetType = assetType)

            AssetVisualHighlight(assetType = assetType, assetName = assetName)

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = assetName,
                    onValueChange = { assetName = it },
                    label = { Text("نام دارایی یا نماد") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoElectric,
                        unfocusedBorderColor = ObsidianSlate700
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PersianNumberTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = "تعداد / مقدار",
                            isDecimalAllowed = true
                        )
                    }
                    Box(modifier = Modifier.weight(1.5f)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("قیمت واحد", style = DaraTypography.labelSmall, color = Slate400)
                                TextButton(onClick = { autoFillPrice() }, contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = EmeraldCore, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("قیمت زنده", style = DaraTypography.labelSmall, color = EmeraldCore)
                                }
                            }
                            PersianNumberTextField(
                                value = unitPriceRial,
                                onValueChange = { unitPriceRial = it },
                                label = "ریال",
                                isDecimalAllowed = true
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSlate800.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.CalendarToday, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                            Text("تاریخ معامله: ${PersianDateUtils.formatJalaliDate(java.util.Date(purchaseDate))}", style = DaraTypography.bodyMedium, color = Slate50)
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, null, tint = IndigoElectric)
                        }
                    }
                }
            }
            
            // Keypad Advice
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Info, null, tint = RefinedAmberGold, modifier = Modifier.size(20.dp))
                    Text(
                        "با ثبت دقیق تاریخ خرید، دارا می‌تواند نرخ تورم را محاسبه کرده و سود واقعی شما را نمایش دهد.",
                        style = DaraTypography.bodySmall,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun AddAssetHeader(onBack: () -> Unit) {
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
                Text("جزئیات معامله", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun ProgressInfoCard(assetType: PortfolioAssetType) {
    Surface(color = ObsidianSlate800, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(36.dp).background(IndigoElectric.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    val icon = when(assetType) {
                        PortfolioAssetType.GOLD -> Icons.Default.BrightnessLow
                        PortfolioAssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
                        PortfolioAssetType.STOCK -> Icons.Default.QueryStats
                        else -> Icons.Default.MonetizationOn
                    }
                    Icon(icon, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                }
                Column {
                    val typeLabel = when(assetType) {
                        PortfolioAssetType.GOLD -> "طلا و سکه"
                        PortfolioAssetType.CRYPTO -> "رمزارز"
                        PortfolioAssetType.STOCK -> "سهام بورسی"
                        else -> "دارایی"
                    }
                    Text("ثبت خرید $typeLabel", style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("گام ۲ از ۲", style = DaraTypography.labelSmall, color = Slate600)
                }
            }
            CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(24.dp), color = EmeraldCore, strokeWidth = 3.dp)
        }
    }
}

@Composable
fun AssetVisualHighlight(assetType: PortfolioAssetType, assetName: String) {
    val icon = when(assetType) {
        PortfolioAssetType.GOLD -> Icons.Default.Token
        PortfolioAssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
        PortfolioAssetType.STOCK -> Icons.Default.Analytics
        else -> Icons.Default.AccountBalanceWallet
    }
    val color = when(assetType) {
        PortfolioAssetType.GOLD -> RefinedAmberGold
        PortfolioAssetType.CRYPTO -> IndigoElectric
        PortfolioAssetType.STOCK -> Color(0xFF8B5CF6)
        else -> EmeraldCore
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate800,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).background(ObsidianSlate700, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(if (assetName.isBlank()) "در حال نام‌گذاری..." else assetName, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text("نرخ لحظه‌ای قابل استعلام", style = DaraTypography.labelSmall, color = EmeraldCore)
            }
        }
    }
}

@Composable
fun SubmitAssetFooter(total: BigDecimal, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = ObsidianSlate900,
        tonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("مبلغ کل خرید:", style = DaraTypography.bodySmall, color = Slate400)
                Text(com.example.util.formatRial(total, isRial = false), style = DaraTypography.titleLarge, color = if (enabled) EmeraldCore else Slate600, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldCore, disabledContainerColor = ObsidianSlate700)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TaskAlt, null, tint = if (enabled) Color.Black else Slate400)
                    Text("ثبت در پورتفوی هوشمند", style = DaraTypography.titleMedium, color = if (enabled) Color.Black else Slate400, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
