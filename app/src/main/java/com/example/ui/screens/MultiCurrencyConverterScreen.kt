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
    
    // Default base is USD, but it can be changed to any currency in the list
    var baseCurrencyCode by remember { mutableStateOf("USD") }
    var inputAmount by remember { mutableStateOf("100") }

    val currencies = listOf(
        "USD" to "دلار آمریکا",
        "EUR" to "یورو",
        "GBP" to "پوند انگلیس",
        "AED" to "درهم امارات",
        "TRY" to "لیر ترکیه",
        "CNY" to "یوآن چین",
        "TOMAN" to "تومان (ایران)"
    )

    // Current Toman value of the base currency
    val baseToTomanRate = if (baseCurrencyCode == "TOMAN") {
        BigDecimal.ONE
    } else {
        marketRates.find { it.assetCode == baseCurrencyCode }?.priceToman ?: BigDecimal.ONE
    }

    val amount = PersianNumberUtils.parseAmount(inputAmount)
    val amountInToman = if (baseCurrencyCode == "TOMAN") amount else amount.multiply(baseToTomanRate)

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("مبدل ارز پیشرفته", fontWeight = FontWeight.Bold) },
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
            // 1. Base Input Section
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("مقدار و ارز مبدأ (جهت تغییر، روی ردیف‌های پایین بزنید)", style = DaraTypography.labelMedium, color = Slate400)
                    
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
                            color = IndigoElectric.copy(alpha = 0.1f),
                            modifier = Modifier.height(56.dp).padding(horizontal = 8.dp),
                            border = BorderStroke(1.dp, IndigoElectric)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(baseCurrencyCode, style = DaraTypography.titleMedium, color = IndigoElectric, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Text("معادل در سایر ارزها", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)

            // 2. Multi-directional List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currencies) { (code, name) ->
                    val targetToTomanRate = if (code == "TOMAN") {
                        BigDecimal.ONE
                    } else {
                        marketRates.find { it.assetCode == code }?.priceToman ?: BigDecimal.ZERO
                    }

                    if (targetToTomanRate > BigDecimal.ZERO || code == "TOMAN") {
                        val converted = if (code == "TOMAN") {
                            amountInToman
                        } else {
                            amountInToman.divide(targetToTomanRate, 4, RoundingMode.HALF_UP)
                        }

                        ClickableConversionItem(
                            currencyName = name,
                            currencyCode = code,
                            value = converted,
                            isBase = code == baseCurrencyCode,
                            onClick = {
                                // Switch base to this currency
                                baseCurrencyCode = code
                                inputAmount = if (code == "TOMAN") converted.toLong().toString() else String.format(Locale.US, "%.2f", converted.toDouble())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClickableConversionItem(
    currencyName: String,
    currencyCode: String,
    value: BigDecimal,
    isBase: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isBase) ObsidianSlate700 else ObsidianSlate800,
        border = BorderStroke(1.dp, if (isBase) IndigoElectric else Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(currencyName, style = DaraTypography.labelSmall, color = Slate400)
                Text(currencyCode, style = DaraTypography.titleMedium, color = if (isBase) IndigoElectric else Slate50, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (currencyCode == "TOMAN") 
                    PersianNumberUtils.formatCurrency(value, isRial = false) 
                    else String.format(Locale.US, "%,.2f", value.toDouble()),
                style = DaraTypography.headlineSmall,
                color = if (currencyCode == "TOMAN") EmeraldCore else if (isBase) IndigoElectric else Slate50,
                fontWeight = FontWeight.Black
            )
        }
    }
}
