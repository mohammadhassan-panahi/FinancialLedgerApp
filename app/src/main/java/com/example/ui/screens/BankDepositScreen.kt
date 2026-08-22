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
fun BankDepositScreen(
    historyList: List<CalculationHistoryEntity>,
    defaultInflation: Double,
    defaultTax: Double,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val isRial = currencyUnit == "ریال"
    val unitLabel = PersianNumberUtils.getCurrencyUnitLabel(isRial)

    val presets = listOf(
        "کوتاه‌مدت (۵٪)" to 5.0,
        "۳ ماهه (۱۲٪)" to 12.0,
        "۶ ماهه (۱۶٪)" to 16.0,
        "۱ ساله (۲۰.۵٪)" to 20.5,
        "۲ ساله (۲۲.۵٪)" to 22.5
    )

    var principalInput by remember { mutableStateOf(if (isRial) "1000000000" else "100000000") }
    var selectedPresetLabel by remember { mutableStateOf("۱ ساله (۲۰.۵٪)") }
    var rateInput by remember { mutableStateOf("20.5") }
    var taxInput by remember { mutableStateOf(defaultTax.toString()) }
    var inflationInput by remember { mutableStateOf(defaultInflation.toString()) }

    var showPrintDialog by remember { mutableStateOf(false) }

    val principal = PersianNumberUtils.parseAmountToToman(principalInput, isRial)
    val rate = rateInput.toDoubleOrNull() ?: 0.0
    val tax = taxInput.toDoubleOrNull() ?: 0.0
    val inflation = inflationInput.toDoubleOrNull() ?: 0.0

    val depositResult = FinancialFormulas.calculateBankDeposit(
        principal = principal,
        annualRatePercent = rate,
        taxRatePercent = tax,
        inflationRatePercent = inflation
    )

    val copySummaryText = """
        گزارش سود سپرده بانکی:
        مبلغ سپرده: ${PersianNumberUtils.formatCurrency(principal, isRial = isRial)}
        نرخ سود سپرده: ${PersianNumberUtils.formatPercent(rate)} سالانه
        سود ماهانه ناخالص: ${PersianNumberUtils.formatCurrency(depositResult.monthlyInterest, isRial = isRial)}
        سود خالص ماهانه (پس از مالیات ${PersianNumberUtils.formatPercent(tax)}): ${PersianNumberUtils.formatCurrency(depositResult.netMonthlyInterest, isRial = isRial)}
        سود واقعی پس از تورم: ${PersianNumberUtils.formatCurrency(depositResult.realInterestAfterInflation, isRial = isRial)}
    """.trimIndent()

    val detailsList = listOf(
        "مبلغ اصل سپرده" to PersianNumberUtils.formatCurrency(principal, isRial = isRial),
        "نرخ سود سالانه" to PersianNumberUtils.formatPercent(rate),
        "سود روزانه" to PersianNumberUtils.formatCurrency(depositResult.dailyInterest, isRial = isRial),
        "سود ماهانه ناخالص" to PersianNumberUtils.formatCurrency(depositResult.monthlyInterest, isRial = isRial),
        "سود سالانه ناخالص" to PersianNumberUtils.formatCurrency(depositResult.yearlyInterest, isRial = isRial),
        "سود خالص ماهانه (پس از مالیات)" to PersianNumberUtils.formatCurrency(depositResult.netMonthlyInterest, isRial = isRial),
        "سود واقعی (با کسر تورم ${PersianNumberUtils.formatPercent(inflation)})" to PersianNumberUtils.formatCurrency(depositResult.realInterestAfterInflation, isRial = isRial)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Presets & Form
        item {
            NotebookCard {
                Text(
                    text = "انتخاب نرخ‌های رایج بانکی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presets.take(3).forEach { (label, r) ->
                        Button(
                            onClick = {
                                selectedPresetLabel = label
                                rateInput = r.toString()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPresetLabel == label) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPresetLabel == label) Color.Black else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presets.drop(3).forEach { (label, r) ->
                        Button(
                            onClick = {
                                selectedPresetLabel = label
                                rateInput = r.toString()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPresetLabel == label) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPresetLabel == label) Color.Black else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PersianNumberTextField(
                    value = principalInput,
                    onValueChange = { principalInput = it },
                    label = "مبلغ سپرده بانکی ($unitLabel)",
                    suffix = unitLabel
                )

                Spacer(modifier = Modifier.height(10.dp))

                PersianNumberTextField(
                    value = rateInput,
                    onValueChange = {
                        rateInput = it
                        selectedPresetLabel = "سفارشی"
                    },
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
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        label = "مالیات بر سود (٪)",
                        suffix = "٪",
                        modifier = Modifier.weight(1f),
                        isDecimalAllowed = true
                    )

                    PersianNumberTextField(
                        value = inflationInput,
                        onValueChange = { inflationInput = it },
                        label = "نرخ تورم سالانه (٪)",
                        suffix = "٪",
                        modifier = Modifier.weight(1f),
                        isDecimalAllowed = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val title = "سپرده - ${PersianNumberUtils.formatCurrency(principal, showSuffix = false)}"
                        val summary = "سود ماهانه: ${PersianNumberUtils.formatCurrency(depositResult.monthlyInterest)}"
                        val params = "$principalInput|$selectedPresetLabel|$rateInput|$taxInput|$inflationInput"
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "deposit",
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

        // Result Banner
        item {
            ResultHeaderBanner(
                title = "سود سپرده بانکی",
                mainResultValue = PersianNumberUtils.formatCurrency(depositResult.monthlyInterest),
                mainResultLabel = "سود ناخالص ماهانه پرداختی بانک",
                secondaryItems = listOf(
                    "سود روزانه" to PersianNumberUtils.formatCurrency(depositResult.dailyInterest),
                    "سود سالانه ناخالص" to PersianNumberUtils.formatCurrency(depositResult.yearlyInterest),
                    "سود خالص ماهانه (پس از مالیات)" to PersianNumberUtils.formatCurrency(depositResult.netMonthlyInterest),
                    "سود واقعی با کسر تورم" to PersianNumberUtils.formatCurrency(depositResult.realInterestAfterInflation)
                ),
                copySummaryText = copySummaryText,
                onPrintClick = { showPrintDialog = true }
            )
        }

        // History Accordion
        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 5) {
                        principalInput = parts[0]
                        selectedPresetLabel = parts[1]
                        rateInput = parts[2]
                        taxInput = parts[3]
                        inflationInput = parts[4]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "سود سپرده بانکی",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
