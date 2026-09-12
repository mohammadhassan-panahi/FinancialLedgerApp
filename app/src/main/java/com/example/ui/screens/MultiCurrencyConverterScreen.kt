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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianNumberUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiCurrencyConverterScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val marketRates by viewModel.marketRates.collectAsStateWithLifecycle()
    var inputAmount by remember { mutableStateOf("100") }
    var baseCurrencyCode by remember { mutableStateOf("USD") }

    val currencies = listOf(
        "USD" to "دلار آمریکا",
        "EUR" to "یورو",
        "GBP" to "پوند انگلیس",
        "AED" to "درهم امارات",
        "TRY" to "لیر ترکیه",
        "CNY" to "یوآن چین",
        "TOMAN" to "تومان (ایران)"
    )

    val baseRate = if (baseCurrencyCode == "TOMAN") {
        BigDecimal.ONE
    } else {
        marketRates.find { it.assetCode == baseCurrencyCode }?.priceToman ?: BigDecimal.ONE
    }

    val amount = PersianNumberUtils.parseAmount(inputAmount)
    val amountInToman = if (baseCurrencyCode == "TOMAN") amount else amount.multiply(baseRate)

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("مبدل ارز جهانی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Input Section
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("انتخاب ارز پایه و مقدار", style = DaraTypography.labelMedium, color = Slate400)
                    
                    // Base Currency Selector (Horizontal List)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(currencies) { (code, _) ->
                            FilterChip(
                                selected = baseCurrencyCode == code,
                                onClick = { baseCurrencyCode = code },
                                label = { Text(code) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoElectric,
                                    selectedLabelColor = Color.White,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            PersianNumberTextField(
                                value = inputAmount,
                                onValueChange = { inputAmount = it },
                                label = "مقدار ورودی",
                                isDecimalAllowed = true
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ObsidianSlate700,
                            modifier = Modifier.height(56.dp).padding(horizontal = 8.dp),
                            border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(baseCurrencyCode, style = DaraTypography.titleMedium, color = IndigoElectric, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Text("معادل در سایر ارزها", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)

            // 2. Results List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currencies) { (code, name) ->
                    if (code != baseCurrencyCode) {
                        val targetRate = if (code == "TOMAN") {
                            BigDecimal.ONE
                        } else {
                            marketRates.find { it.assetCode == code }?.priceToman ?: BigDecimal.ZERO
                        }

                        if (targetRate > BigDecimal.ZERO || code == "TOMAN") {
                            val converted = if (code == "TOMAN") {
                                amountInToman
                            } else {
                                amountInToman.divide(targetRate, 4, RoundingMode.HALF_UP)
                            }

                            ConversionResultItem(
                                currencyName = name,
                                currencyCode = code,
                                value = converted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversionResultItem(currencyName: String, currencyCode: String, value: BigDecimal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSlate800,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(currencyName, style = DaraTypography.labelSmall, color = Slate400)
                Text(currencyCode, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (currencyCode == "TOMAN") 
                    PersianNumberUtils.formatCurrency(value, isRial = false) 
                    else String.format(Locale.US, "%,.2f", value.toDouble()),
                style = DaraTypography.headlineSmall,
                color = if (currencyCode == "TOMAN") EmeraldCore else Slate50,
                fontWeight = FontWeight.Black
            )
        }
    }
}
