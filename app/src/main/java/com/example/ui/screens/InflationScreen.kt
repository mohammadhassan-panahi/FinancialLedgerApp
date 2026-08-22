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
import com.example.data.local.CalculationHistoryEntity
import com.example.ui.components.BarChartItem
import com.example.ui.components.ComposeBarChart
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.components.PrintPdfDialog
import com.example.ui.components.ResultHeaderBanner
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.util.FinancialFormulas
import com.example.util.PersianNumberUtils

@Composable
fun InflationScreen(
    historyList: List<CalculationHistoryEntity>,
    defaultInflation: Double,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val isRial = currencyUnit == "ریال"
    val unitLabel = PersianNumberUtils.getCurrencyUnitLabel(isRial)

    var amountInput by remember { mutableStateOf(if (isRial) "10000000000" else "1000000000") } // 1 Billion
    var inflationInput by remember { mutableStateOf(defaultInflation.toString()) }
    var yearsInput by remember { mutableStateOf("5") }

    var showPrintDialog by remember { mutableStateOf(false) }

    val amount = PersianNumberUtils.parseAmountToToman(amountInput, isRial)
    val inflation = inflationInput.toDoubleOrNull() ?: 0.0
    val years = yearsInput.toIntOrNull() ?: 1

    val infResult = FinancialFormulas.calculateInflation(
        currentAmount = amount,
        annualInflationPercent = inflation,
        years = years
    )

    val copySummaryText = """
        گزارش اثر تورم و افت قدرت خرید:
        مبلغ امروز: ${PersianNumberUtils.formatCurrency(amount, isRial = isRial)}
        نرخ تورم سالانه: ${PersianNumberUtils.formatPercent(inflation)} | مدت: ${PersianNumberUtils.toPersianDigits(yearsInput)} سال
        ارزش واقعی در آینده: ${PersianNumberUtils.formatCurrency(infResult.futureRealPurchasingPower, isRial = isRial)}
        افت قدرت خرید: ${PersianNumberUtils.formatPercent(infResult.percentageLoss)}
        مبلغ معادل مورد نیاز برای حفظ قدرت خرید: ${PersianNumberUtils.formatCurrency(infResult.futureAmountNeededToMatchToday, isRial = isRial)}
    """.trimIndent()

    val detailsList = listOf(
        "مبلغ امروز" to PersianNumberUtils.formatCurrency(amount, isRial = isRial),
        "نرخ تورم سالانه" to PersianNumberUtils.formatPercent(inflation),
        "مدت زمان" to "${PersianNumberUtils.toPersianDigits(yearsInput)} سال",
        "ارزش واقعی این مبلغ در آینده" to PersianNumberUtils.formatCurrency(infResult.futureRealPurchasingPower, isRial = isRial),
        "درصد افت قدرت خرید" to PersianNumberUtils.formatPercent(infResult.percentageLoss),
        "مبلغ معادل مورد نیاز در آینده" to PersianNumberUtils.formatCurrency(infResult.futureAmountNeededToMatchToday, isRial = isRial)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form
        item {
            NotebookCard {
                Text(
                    text = "محاسبه تورم و قدرت خرید",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )

                Spacer(modifier = Modifier.height(14.dp))

                PersianNumberTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = "مبلغ امروز ($unitLabel)",
                    suffix = unitLabel
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianNumberTextField(
                        value = inflationInput,
                        onValueChange = { inflationInput = it },
                        label = "نرخ تورم سالانه (٪)",
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

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val title = "تورم - ${PersianNumberUtils.formatCurrency(amount, showSuffix = false)}"
                        val summary = "افت قدرت خرید: ${PersianNumberUtils.formatPercent(infResult.percentageLoss)}"
                        val params = "$amountInput|$inflationInput|$yearsInput"
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "inflation",
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
                title = "نتیجه اثر تورم بر سرمایه شما",
                mainResultValue = PersianNumberUtils.formatCurrency(infResult.futureRealPurchasingPower),
                mainResultLabel = "ارزش واقعی سرمایه شما در ${PersianNumberUtils.toPersianDigits(yearsInput)} سال آینده (بر حسب قدرت خرید امروز)",
                secondaryItems = listOf(
                    "درصد کاهش قدرت خرید" to PersianNumberUtils.formatPercent(infResult.percentageLoss),
                    "مبلغ معادل لازم در آینده جهت حفظ قدرت خرید" to PersianNumberUtils.formatCurrency(infResult.futureAmountNeededToMatchToday)
                ),
                copySummaryText = copySummaryText,
                onPrintClick = { showPrintDialog = true }
            )
        }

        // Bar Chart
        if (infResult.yearlyLossBreakdown.isNotEmpty()) {
            item {
                NotebookCard {
                    Text(
                        text = "نمودار میله‌ای افت سالانه قدرت خرید",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val barItems = infResult.yearlyLossBreakdown.map { row ->
                        BarChartItem(
                            label = "سال ${PersianNumberUtils.toPersianDigits(row.year.toString())}",
                            value = row.purchasingPowerValue,
                            color = LossRed.copy(alpha = 0.8f)
                        )
                    }

                    ComposeBarChart(items = barItems)
                }
            }
        }

        // History Accordion
        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 3) {
                        amountInput = parts[0]
                        inflationInput = parts[1]
                        yearsInput = parts[2]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "تورم و قدرت خرید",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
