package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationHistoryEntity
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.components.PrintPdfDialog
import com.example.ui.components.ResultHeaderBanner
import com.example.ui.theme.AccentGold
import com.example.util.GoldMarketFormulas
import com.example.util.PersianNumberUtils

/**
 * اجرت و مالیات طلا — final purchase cost per the اتحادیه طلا formula for new
 * (base + wage + shop profit, VAT on wage+profit) and second-hand gold
 * (base + wage, VAT on wage only).
 */
@Composable
fun GoldWageScreen(
    historyList: List<CalculationHistoryEntity>,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    var isNew by remember { mutableStateOf(true) }
    var basePriceInput by remember { mutableStateOf("50000000") }
    var weightInput by remember { mutableStateOf("5") }
    var wageInput by remember { mutableStateOf("7") }
    var profitInput by remember { mutableStateOf("7") }
    var vatInput by remember { mutableStateOf("9") }
    var showPrintDialog by remember { mutableStateOf(false) }

    val base = PersianNumberUtils.parseAmount(basePriceInput)
    val weight = PersianNumberUtils.parseAmount(weightInput)
    val wage = PersianNumberUtils.parseAmount(wageInput)
    val profit = PersianNumberUtils.parseAmount(profitInput)
    val vat = PersianNumberUtils.parseAmount(vatInput)

    val result = GoldMarketFormulas.calculateGoldPurchasePrice(
        basePricePerGram = base,
        weightGrams = weight,
        wagePercent = wage,
        sellerProfitPercent = profit,
        vatPercent = vat,
        isNew = isNew
    )

    val summaryText = result?.let {
        """
            گزارش قیمت تمام‌شده طلا (${if (isNew) "نو" else "دست‌دوم"}):
            مظنه هر گرم: ${PersianNumberUtils.formatCurrency(base, showSuffix = false)} تومان | وزن: ${PersianNumberUtils.toPersianDigits(weightInput)} گرم
            اجرت هر گرم: ${PersianNumberUtils.formatCurrency(it.wagePerGram, showSuffix = false)} تومان
            ${if (isNew) "سود فروشنده هر گرم: ${PersianNumberUtils.formatCurrency(it.sellerProfitPerGram, showSuffix = false)} تومان" else "سود فروشنده: ندارد (دست‌دوم)"}
            مالیات هر گرم: ${PersianNumberUtils.formatCurrency(it.vatPerGram, showSuffix = false)} تومان
            قیمت تمام‌شده هر گرم: ${PersianNumberUtils.formatCurrency(it.finalPricePerGram, showSuffix = false)} تومان
            قیمت تمام‌شده کل: ${PersianNumberUtils.formatCurrency(it.finalPriceTotal, showSuffix = false)} تومان
        """.trimIndent()
    } ?: ""

    val detailsList = result?.let {
        listOf(
            "مظنه (ارزش ذاتی) هر گرم" to PersianNumberUtils.formatCurrency(it.basePricePerGram, showSuffix = false),
            "اجرت ساخت هر گرم" to PersianNumberUtils.formatCurrency(it.wagePerGram, showSuffix = false),
            "سود فروشنده هر گرم" to (if (isNew) PersianNumberUtils.formatCurrency(it.sellerProfitPerGram, showSuffix = false) else "ندارد"),
            "مالیات بر ارزش افزوده هر گرم" to PersianNumberUtils.formatCurrency(it.vatPerGram, showSuffix = false),
            "قیمت تمام‌شده هر گرم" to PersianNumberUtils.formatCurrency(it.finalPricePerGram, showSuffix = false),
            "قیمت تمام‌شده ${PersianNumberUtils.toPersianDigits(weightInput)} گرم" to PersianNumberUtils.formatCurrency(it.finalPriceTotal, showSuffix = false)
        )
    } ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NotebookCard {
                Text(
                    "نوع خرید",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isNew = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNew) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "طلای نو (فروشگاهی)",
                            color = if (isNew) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = { isNew = false },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isNew) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "دست‌دوم (از شخص)",
                            color = if (!isNew) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            NotebookCard {
                PersianNumberTextField(
                    value = basePriceInput,
                    onValueChange = { basePriceInput = it },
                    label = "مظنه / ارزش ذاتی هر گرم (تومان)",
                    suffix = "تومان"
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = "وزن (گرم)",
                    suffix = "گرم",
                    isDecimalAllowed = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = wageInput,
                    onValueChange = { wageInput = it },
                    label = "اجرت ساخت (٪ مظنه)",
                    suffix = "٪",
                    isDecimalAllowed = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = profitInput,
                    onValueChange = { profitInput = it },
                    label = "سود فروشنده (٪ — فقط طلای نو)",
                    suffix = "٪",
                    isDecimalAllowed = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = vatInput,
                    onValueChange = { vatInput = it },
                    label = "مالیات بر ارزش افزوده (٪)",
                    suffix = "٪",
                    isDecimalAllowed = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        result ?: return@Button
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "gold_wage",
                                title = "قیمت تمام‌شده ${if (isNew) "طلای نو" else "طلای دست‌دوم"}",
                                summary = "هر گرم: ${PersianNumberUtils.formatCurrency(result.finalPricePerGram, showSuffix = false)} | کل: ${PersianNumberUtils.formatCurrency(result.finalPriceTotal, showSuffix = false)}",
                                paramsJson = "$isNew|$basePriceInput|$weightInput|$wageInput|$profitInput|$vatInput"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    enabled = result != null
                ) {
                    Text("ذخیره در تاریخچه", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (result != null) {
            item {
                ResultHeaderBanner(
                    title = "قیمت تمام‌شده طلا (${if (isNew) "نو" else "دست‌دوم"})",
                    mainResultValue = PersianNumberUtils.formatCurrency(result.finalPriceTotal, showSuffix = false),
                    mainResultLabel = "کل خرید ${PersianNumberUtils.toPersianDigits(weightInput)} گرم",
                    secondaryItems = listOf(
                        "قیمت تمام‌شده هر گرم" to PersianNumberUtils.formatCurrency(result.finalPricePerGram, showSuffix = false),
                        "اجرت + مالیات هر گرم" to PersianNumberUtils.formatCurrency(result.wagePerGram + result.sellerProfitPerGram + result.vatPerGram, showSuffix = false)
                    ),
                    copySummaryText = summaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            }
        }

        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 6) {
                        isNew = parts[0].toBooleanStrictOrNull() ?: true
                        basePriceInput = parts[1]
                        weightInput = parts[2]
                        wageInput = parts[3]
                        profitInput = parts[4]
                        vatInput = parts[5]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog && result != null) {
        PrintPdfDialog(
            sectionTitle = "اجرت و مالیات طلا",
            summaryContent = summaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
