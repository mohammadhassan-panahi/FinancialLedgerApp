package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.PortfolioAssetType
import com.example.ui.theme.*
import com.example.ui.components.PersianNumberTextField
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianNumberUtils
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(viewModel: PortfolioViewModel) {
    var assetType by remember { mutableStateOf(PortfolioAssetType.GOLD) }
    var assetCode by remember { mutableStateOf("GOLD_18K") }
    var assetName by remember { mutableStateOf("طلا ۱۸ عیار (گرم)") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
    val cryptoAssets by viewModel.cryptoAssets.collectAsStateWithLifecycle()
    val mutualFunds by viewModel.mutualFunds.collectAsStateWithLifecycle()
    
    val usdRateToman = marketRates.find { it.assetCode == "USD" }?.priceToman ?: 65000.0
    val rialPerToman = 10.0

    fun updateUnitPriceFromMarket() {
        val livePriceRial = when (assetType) {
            PortfolioAssetType.GOLD -> marketRates.find { it.assetCode == assetCode }?.priceToman?.let { it * rialPerToman }
            PortfolioAssetType.USD -> marketRates.find { it.assetCode == assetCode }?.priceToman?.let { it * rialPerToman }
            PortfolioAssetType.CRYPTO -> cryptoAssets.find { it.symbol == assetCode }?.priceUsd?.let { it * usdRateToman * rialPerToman }
            PortfolioAssetType.FUND -> mutualFunds.find { it.id == assetCode }?.navToman?.let { it * rialPerToman }
            PortfolioAssetType.STOCK -> null // Tsetmc doesn't easily map to purchase screen without a search
            PortfolioAssetType.CASH -> 1.0
        }
        
        if (livePriceRial != null && livePriceRial > 0) {
            unitPrice = livePriceRial.toLong().toString()
        }
    }

    // Auto-update price when asset selection changes
    LaunchedEffect(assetType, assetCode) {
        updateUnitPriceFromMarket()
    }

    fun selectType(type: PortfolioAssetType) {
        assetType = type
        when (type) {
            PortfolioAssetType.GOLD -> { assetCode = "GOLD_18K"; assetName = "طلا ۱۸ عیار (گرم)" }
            PortfolioAssetType.USD -> { assetCode = "USD"; assetName = "دلار آمریکا" }
            PortfolioAssetType.STOCK -> { assetCode = ""; assetName = "" }
            PortfolioAssetType.CASH -> { assetCode = "CASH_RIAL"; assetName = "نقدینگی (ریال)" }
            PortfolioAssetType.CRYPTO -> { assetCode = ""; assetName = "" }
            PortfolioAssetType.FUND -> { assetCode = ""; assetName = "" }
        }
    }

    Scaffold(
        containerColor = DarkSlateSurface
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "ثبت دارایی جدید",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "اطلاعات خرید خود را وارد کنید",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorderLight)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("نوع دارایی", color = TextSecondary, fontSize = 14.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                PortfolioAssetType.GOLD to "طلا",
                                PortfolioAssetType.USD to "ارز",
                                PortfolioAssetType.STOCK to "بورس",
                                PortfolioAssetType.CRYPTO to "کریپتو",
                                PortfolioAssetType.FUND to "صندوق"
                            ).forEach { (type, label) ->
                                FilterChip(
                                    selected = assetType == type,
                                    onClick = { selectType(type) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CredifyIndigo,
                                        selectedLabelColor = Color.White,
                                        labelColor = TextSecondary,
                                        containerColor = DarkSlateTertiary
                                    ),
                                    border = null
                                )
                            }
                        }

                        if (assetType == PortfolioAssetType.STOCK || assetType == PortfolioAssetType.CRYPTO || assetType == PortfolioAssetType.FUND) {
                            OutlinedTextField(
                                value = assetCode,
                                onValueChange = { assetCode = it; assetName = it },
                                label = { 
                                    Text(when(assetType) {
                                        PortfolioAssetType.STOCK -> "نماد بورسی (فولاد، خودرو...)"
                                        PortfolioAssetType.CRYPTO -> "نماد رمزارز (BTC, ETH...)"
                                        else -> "شناسه صندوق (ETEMAD, MOFID...)"
                                    })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = CredifyIndigo,
                                    unfocusedBorderColor = SlateBorderLight
                                )
                            )
                        }

                        PersianNumberTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = when (assetType) {
                                PortfolioAssetType.STOCK -> "تعداد سهم"
                                PortfolioAssetType.GOLD -> "مقدار (گرم)"
                                PortfolioAssetType.USD -> "مقدار (واحد ارز)"
                                PortfolioAssetType.CASH -> "مقدار (ریال)"
                                PortfolioAssetType.CRYPTO -> "مقدار (واحد)"
                                PortfolioAssetType.FUND -> "تعداد واحد صندوق"
                            },
                            isDecimalAllowed = true
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("قیمت واحد (خرید)", color = TextSecondary, fontSize = 14.sp)
                                TextButton(
                                    onClick = { updateUnitPriceFromMarket() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.AutoGraph, null, modifier = Modifier.size(16.dp), tint = EmeraldProfit)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("دریافت قیمت روز", fontSize = 12.sp, color = EmeraldProfit)
                                }
                            }
                            PersianNumberTextField(
                                value = unitPrice,
                                onValueChange = { unitPrice = it },
                                label = "قیمت به ریال",
                                suffix = "ریال"
                            )
                        }

                        // Total Display
                        val q = PersianNumberUtils.parseAmount(quantity)
                        val p = PersianNumberUtils.parseAmount(unitPrice)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSlateTertiary.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مجموع کل:", color = TextSecondary)
                                Text(formatRial(q * p), fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Button(
                            enabled = q > 0 && p > 0 && assetCode.isNotBlank(),
                            onClick = {
                                viewModel.addPurchase(
                                    assetType = assetType,
                                    assetCode = assetCode,
                                    assetName = assetName.ifBlank { assetCode },
                                    quantity = q,
                                    unitPriceRial = p,
                                    purchaseDate = System.currentTimeMillis()
                                )
                                quantity = ""
                                unitPrice = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CredifyIndigo)
                        ) {
                            Text("ثبت در پورتفو", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "آخرین تراکنش‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (purchases.isEmpty()) {
                item {
                    Text("تاریخچه خریدها خالی است.", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                items(purchases.take(10)) { purchase ->
                    PurchaseRowPremium(purchase, onDelete = { viewModel.deletePurchase(purchase.id) })
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PurchaseRowPremium(purchase: AssetPurchaseEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(purchase.assetName, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "${com.example.util.PersianNumberUtils.formatDecimal(purchase.quantity)} واحد - ${formatRial(purchase.unitPriceRial)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = RoseLoss.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }
}
