package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.components.ComposeLineChart
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.LineChartSeries
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.components.PrintPdfDialog
import com.example.ui.components.ResultHeaderBanner
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.util.FinancialFormulas
import com.example.util.PersianNumberUtils

@Composable
fun CompoundInterestScreen(
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

    var isTargetMode by remember { mutableStateOf(false) } // false: Final Value, true: Target Reverse Mode

    var initialPrincipalInput by remember { mutableStateOf(if (isRial) "500000000" else "50000000") }
    var monthlyDepositInput by remember { mutableStateOf(if (isRial) "50000000" else "5000000") }
    var targetValueInput by remember { mutableStateOf(if (isRial) "10000000000" else "1000000000") }
    var rateInput by remember { mutableStateOf("25") }
    var yearsInput by remember { mutableStateOf("5") }
    var inflationInput by remember { mutableStateOf(defaultInflation.toString()) }
    var taxInput by remember { mutableStateOf(defaultTax.toString()) }

    var showPrintDialog by remember { mutableStateOf(false) }

    val initialP = PersianNumberUtils.parseAmountToToman(initialPrincipalInput, isRial)
    val monthlyP = PersianNumberUtils.parseAmountToToman(monthlyDepositInput, isRial)
    val targetVal = PersianNumberUtils.parseAmountToToman(targetValueInput, isRial)
    val rate = rateInput.toDoubleOrNull() ?: 0.0
    val years = yearsInput.toIntOrNull() ?: 1
    val inflation = inflationInput.toDoubleOrNull() ?: 0.0
    val tax = taxInput.toDoubleOrNull() ?: 0.0

    // Calculations
    val compResult = FinancialFormulas.calculateCompoundInterest(
        initialPrincipal = initialP,
        monthlyDeposit = if (isTargetMode) 0.0 else monthlyP,
        annualRatePercent = rate,
        years = years,
        compoundingFrequency = "monthly",
        inflationRatePercent = inflation,
        taxRatePercent = tax
    )

    val requiredMonthlyP = if (isTargetMode) {
        FinancialFormulas.calculateRequiredMonthlyDepositForTarget(
            initialPrincipal = initialP,
            targetFinalValue = targetVal,
            annualRatePercent = rate,
            years = years
        )
    } else 0.0

    val copySummaryText = if (!isTargetMode) {
        """
            گزارش سود مرکب:
            مبلغ اولیه: ${PersianNumberUtils.formatCurrency(initialP, isRial = isRial)}
            واریز ماهانه: ${PersianNumberUtils.formatCurrency(monthlyP, isRial = isRial)}
            نرخ سود سالانه: ${PersianNumberUtils.formatPercent(rate)}
            مدت: ${PersianNumberUtils.toPersianDigits(yearsInput)} سال
            مجموع واریزی: ${PersianNumberUtils.formatCurrency(compResult.totalDeposited, isRial = isRial)}
            سود ناخالص: ${PersianNumberUtils.formatCurrency(compResult.grossInterest, isRial = isRial)}
            ارزش نهایی اسمی: ${PersianNumberUtils.formatCurrency(compResult.finalNominalValue, isRial = isRial)}
            ارزش واقعی (پس از کسر تورم ${PersianNumberUtils.formatPercent(inflation)}): ${PersianNumberUtils.formatCurrency(compResult.finalRealValueInflationAdjusted, isRial = isRial)}
        """.trimIndent()
    } else {
        """
            گزارش حالت معکوس سود مرکب (محاسبه واریزی ماهانه):
            مبلغ اولیه: ${PersianNumberUtils.formatCurrency(initialP, isRial = isRial)}
            مبلغ هدف نهایی: ${PersianNumberUtils.formatCurrency(targetVal, isRial = isRial)}
            نرخ سود سالانه: ${PersianNumberUtils.formatPercent(rate)} | مدت: ${PersianNumberUtils.toPersianDigits(yearsInput)} سال
            واریز ماهانه مورد نیاز: ${PersianNumberUtils.formatCurrency(requiredMonthlyP, isRial = isRial)}
        """.trimIndent()
    }

    val detailsList = if (!isTargetMode) {
        listOf(
            "مبلغ اولیه" to PersianNumberUtils.formatCurrency(initialP, isRial = isRial),
            "واریز ماهانه" to PersianNumberUtils.formatCurrency(monthlyP, isRial = isRial),
            "نرخ سود سالانه" to PersianNumberUtils.formatPercent(rate),
            "مدت سرمایه‌گذاری" to "${PersianNumberUtils.toPersianDigits(yearsInput)} سال",
            "مجموع کل اصل واریزی" to PersianNumberUtils.formatCurrency(compResult.totalDeposited, isRial = isRial),
            "سود ناخالص کسب شده" to PersianNumberUtils.formatCurrency(compResult.grossInterest, isRial = isRial),
            "ارزش نهایی اسمی" to PersianNumberUtils.formatCurrency(compResult.finalNominalValue, isRial = isRial),
            "ارزش واقعی (پس از تورم)" to PersianNumberUtils.formatCurrency(compResult.finalRealValueInflationAdjusted, isRial = isRial)
        )
    } else {
        listOf(
            "مبلغ اولیه" to PersianNumberUtils.formatCurrency(initialP, isRial = isRial),
            "مبلغ هدف نهایی" to PersianNumberUtils.formatCurrency(targetVal, isRial = isRial),
            "نرخ سود سالانه" to PersianNumberUtils.formatPercent(rate),
            "مدت زمان" to "${PersianNumberUtils.toPersianDigits(yearsInput)} سال",
            "واریز ماهانه مورد نیاز" to PersianNumberUtils.formatCurrency(requiredMonthlyP, isRial = isRial)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Switcher
        item {
            NotebookCard {
                Text(
                    text = "حالت محاسبه سود مرکب",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isTargetMode = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isTargetMode) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "ارزش نهایی",
                            color = if (!isTargetMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { isTargetMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTargetMode) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "حالت معکوس (محاسبه پس‌انداز)",
                            color = if (isTargetMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Form Inputs
        item {
            NotebookCard {
                PersianNumberTextField(
                    value = initialPrincipalInput,
                    onValueChange = { initialPrincipalInput = it },
                    label = "مبلغ اولیه سرمایه ($unitLabel)",
                    suffix = unitLabel
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (!isTargetMode) {
                    PersianNumberTextField(
                        value = monthlyDepositInput,
                        onValueChange = { monthlyDepositInput = it },
                        label = "واریزی ماهانه ($unitLabel)",
                        suffix = unitLabel
                    )
                } else {
                    PersianNumberTextField(
                        value = targetValueInput,
                        onValueChange = { targetValueInput = it },
                        label = "مبلغ هدف نهایی ($unitLabel)",
                        suffix = unitLabel
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianNumberTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = "نرخ سود سالانه (٪)",
                        suffix = "٪",
                        modifier = Modifier.weight(1f),
                        isDecimalAllowed = true
                    )

                    PersianNumberTextField(
                        value = yearsInput,
                        onValueChange = { yearsInput = it },
                        label = "مدت (سال)",
                        suffix = "سال",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Inflation & Tax Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianNumberTextField(
                        value = inflationInput,
                        onValueChange = { inflationInput = it },
                        label = "نرخ تورم سالانه (اختیاری)",
                        suffix = "٪",
                        modifier = Modifier.weight(1f),
                        isDecimalAllowed = true
                    )

                    PersianNumberTextField(
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        label = "مالیات بر سود (اختیاری)",
                        suffix = "٪",
                        modifier = Modifier.weight(1f),
                        isDecimalAllowed = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val title = if (!isTargetMode) "سود مرکب - ارزش نهایی" else "سود مرکب - حالت معکوس"
                        val summary = if (!isTargetMode) "نهایی: ${PersianNumberUtils.formatCurrency(compResult.finalNominalValue)}" else "واریز ماهانه: ${PersianNumberUtils.formatCurrency(requiredMonthlyP)}"
                        val params = "$isTargetMode|$initialPrincipalInput|$monthlyDepositInput|$targetValueInput|$rateInput|$yearsInput|$inflationInput|$taxInput"
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "compound",
                                title = title,
                                summary = summary,
                                paramsJson = params
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text("ذخیره در تاریخچه", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Result Banner
        item {
            if (!isTargetMode) {
                ResultHeaderBanner(
                    title = "نتیجه محاسبات سود مرکب",
                    mainResultValue = PersianNumberUtils.formatCurrency(compResult.finalNominalValue),
                    mainResultLabel = "ارزش نهایی اسمی سرمایه",
                    secondaryItems = listOf(
                        "مجموع اصل واریزی" to PersianNumberUtils.formatCurrency(compResult.totalDeposited),
                        "سود ناخالص" to PersianNumberUtils.formatCurrency(compResult.grossInterest),
                        "ارزش واقعی پس از تورم" to PersianNumberUtils.formatCurrency(compResult.finalRealValueInflationAdjusted)
                    ),
                    copySummaryText = copySummaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            } else {
                ResultHeaderBanner(
                    title = "نتیجه حالت معکوس (پس‌انداز هدف)",
                    mainResultValue = PersianNumberUtils.formatCurrency(requiredMonthlyP),
                    mainResultLabel = "واریز ماهانه مورد نیاز برای رسیدن به هدف",
                    secondaryItems = listOf(
                        "مبلغ هدف نهایی" to PersianNumberUtils.formatCurrency(targetVal),
                        "مبلغ اولیه" to PersianNumberUtils.formatCurrency(initialP)
                    ),
                    copySummaryText = copySummaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            }
        }

        // Line Chart Growth
        if (!isTargetMode && compResult.yearlyBreakdown.isNotEmpty()) {
            item {
                NotebookCard {
                    Text(
                        text = "نمودار رشد سرمایه مرکب در طول زمان",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val nominalPoints = compResult.yearlyBreakdown.map { Pair(it.year.toDouble(), it.endingBalance) }
                    val realPoints = compResult.yearlyBreakdown.map { Pair(it.year.toDouble(), it.realValueInflationAdjusted) }

                    ComposeLineChart(
                        seriesList = listOf(
                            LineChartSeries("ارزش اسمی", nominalPoints, AccentGold),
                            LineChartSeries("ارزش واقعی (پس از تورم)", realPoints, ProfitGreen, isDotted = true)
                        ),
                        xAxisLabel = "سال"
                    )
                }
            }

            // Annual Breakdown Table
            item {
                NotebookCard {
                    Text(
                        text = "جدول تفکیک سالانه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    compResult.yearlyBreakdown.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سال ${PersianNumberUtils.toPersianDigits(row.year.toString())}", fontWeight = FontWeight.Bold)
                            Text("موجودی: ${PersianNumberUtils.formatCurrency(row.endingBalance)}", style = MaterialTheme.typography.bodySmall)
                            Text("واقعی: ${PersianNumberUtils.formatCurrency(row.realValueInflationAdjusted)}", style = MaterialTheme.typography.bodySmall, color = ProfitGreen)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // History Accordion
        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 8) {
                        isTargetMode = parts[0].toBooleanStrictOrNull() ?: false
                        initialPrincipalInput = parts[1]
                        monthlyDepositInput = parts[2]
                        targetValueInput = parts[3]
                        rateInput = parts[4]
                        yearsInput = parts[5]
                        inflationInput = parts[6]
                        taxInput = parts[7]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "سود مرکب",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
