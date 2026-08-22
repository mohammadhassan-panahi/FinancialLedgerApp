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
import com.example.util.FinancialFormulas
import com.example.util.PersianNumberUtils

@Composable
fun SimpleInterestScreen(
    historyList: List<CalculationHistoryEntity>,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val isRial = currencyUnit == "ریال"
    val unitLabel = PersianNumberUtils.getCurrencyUnitLabel(isRial)

    var principalInput by remember { mutableStateOf(if (isRial) "1000000000" else "100000000") } // Default
    var rateInput by remember { mutableStateOf("22") } // 22%
    var durationValueInput by remember { mutableStateOf("12") }
    var durationType by remember { mutableStateOf("months") } // "days", "months", "years"

    var showPrintDialog by remember { mutableStateOf(false) }

    val principal = PersianNumberUtils.parseAmountToToman(principalInput, isRial)
    val rate = rateInput.toDoubleOrNull() ?: 0.0
    val durationVal = durationValueInput.toDoubleOrNull() ?: 0.0

    val result = FinancialFormulas.calculateSimpleInterest(
        principal = principal,
        annualRatePercent = rate,
        durationValue = durationVal,
        durationType = durationType
    )

    val copySummaryText = """
        گزارش سود ساده:
        مبلغ سرمایه: ${PersianNumberUtils.formatCurrency(principal, isRial = isRial)}
        نرخ سود: ${PersianNumberUtils.formatPercent(rate)} سالانه
        مدت: ${PersianNumberUtils.toPersianDigits(durationValueInput)} $durationType
        سود ماهانه: ${PersianNumberUtils.formatCurrency(result.monthlyInterest, isRial = isRial)}
        سود کل بازه: ${PersianNumberUtils.formatCurrency(result.totalInterest, isRial = isRial)}
        مجموع کل (اصل + سود): ${PersianNumberUtils.formatCurrency(result.totalAmount, isRial = isRial)}
    """.trimIndent()

    val detailsList = listOf(
        "مبلغ اولیه سرمایه" to PersianNumberUtils.formatCurrency(principal, isRial = isRial),
        "نرخ سود سالانه" to PersianNumberUtils.formatPercent(rate),
        "سود روزانه" to PersianNumberUtils.formatCurrency(result.dailyInterest, isRial = isRial),
        "سود ماهانه" to PersianNumberUtils.formatCurrency(result.monthlyInterest, isRial = isRial),
        "سود سالانه" to PersianNumberUtils.formatCurrency(result.yearlyInterest, isRial = isRial),
        "سود کل بازه" to PersianNumberUtils.formatCurrency(result.totalInterest, isRial = isRial),
        "مجموع پرداختی (اصل + سود)" to PersianNumberUtils.formatCurrency(result.totalAmount, isRial = isRial)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NotebookCard {
                Text(
                    text = "محاسبه سود ساده",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )

                Spacer(modifier = Modifier.height(14.dp))

                PersianNumberTextField(
                    value = principalInput,
                    onValueChange = { principalInput = it },
                    label = "مبلغ سرمایه اولیه ($unitLabel)",
                    suffix = unitLabel
                )

                Spacer(modifier = Modifier.height(10.dp))

                PersianNumberTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = "نرخ سود سالانه (٪)",
                    suffix = "٪",
                    isDecimalAllowed = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianNumberTextField(
                        value = durationValueInput,
                        onValueChange = { durationValueInput = it },
                        label = "مدت زمان",
                        modifier = Modifier.weight(1.5f),
                        isDecimalAllowed = true
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("واحد مدت", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("days" to "روز", "months" to "ماه", "years" to "سال").forEach { (typeKey, label) ->
                                Button(
                                    onClick = { durationType = typeKey },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (durationType == typeKey) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (durationType == typeKey) Color.Black else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val title = "سود ساده - ${PersianNumberUtils.formatCurrency(principal, showSuffix = false)}"
                        val summary = "سود کل: ${PersianNumberUtils.formatCurrency(result.totalInterest)}"
                        val params = "$principalInput|$rateInput|$durationValueInput|$durationType"
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "simple_interest",
                                title = title,
                                summary = summary,
                                paramsJson = params
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text(
                        text = "ذخیره در تاریخچه",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        item {
            ResultHeaderBanner(
                title = "نتیجه محاسبات سود ساده",
                mainResultValue = PersianNumberUtils.formatCurrency(result.totalAmount),
                mainResultLabel = "مجموع اصل سرمایه + سود کل بازه",
                secondaryItems = listOf(
                    "سود ماهانه" to PersianNumberUtils.formatCurrency(result.monthlyInterest),
                    "سود کل بازه" to PersianNumberUtils.formatCurrency(result.totalInterest),
                    "سود روزانه" to PersianNumberUtils.formatCurrency(result.dailyInterest)
                ),
                copySummaryText = copySummaryText,
                onPrintClick = { showPrintDialog = true }
            )
        }

        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 4) {
                        principalInput = parts[0]
                        rateInput = parts[1]
                        durationValueInput = parts[2]
                        durationType = parts[3]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "سود ساده",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
