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
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.components.PrintPdfDialog
import com.example.ui.components.ResultHeaderBanner
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ProfitGreen
import com.example.util.FinancialFormulas
import com.example.util.PersianNumberUtils

@Composable
fun LoanCalculatorScreen(
    historyList: List<CalculationHistoryEntity>,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val isRial = currencyUnit == "ریال"
    val unitLabel = PersianNumberUtils.getCurrencyUnitLabel(isRial)

    var isReverseMode by remember { mutableStateOf(false) }

    var loanAmountInput by remember { mutableStateOf(if (isRial) "2000000000" else "200000000") } // Default
    var desiredPaymentInput by remember { mutableStateOf(if (isRial) "80000000" else "8000000") }
    var rateInput by remember { mutableStateOf("23") } // 23%
    var durationMonthsInput by remember { mutableStateOf("36") } // 36 months
    var initialFeePercentInput by remember { mutableStateOf("1") } // 1%
    var earlySettlementMonthInput by remember { mutableStateOf("0") }
    var penaltyPercentInput by remember { mutableStateOf("2") }

    var showPrintDialog by remember { mutableStateOf(false) }

    val loanAmt = PersianNumberUtils.parseAmountToToman(loanAmountInput, isRial)
    val desiredPmt = PersianNumberUtils.parseAmountToToman(desiredPaymentInput, isRial)
    val rate = rateInput.toDoubleOrNull() ?: 0.0
    val months = durationMonthsInput.toIntOrNull() ?: 12
    val feePct = initialFeePercentInput.toDoubleOrNull() ?: 0.0
    val settlementMonth = earlySettlementMonthInput.toIntOrNull() ?: 0
    val penaltyPct = penaltyPercentInput.toDoubleOrNull() ?: 0.0

    val loanResult = FinancialFormulas.calculateLoan(
        loanAmount = loanAmt,
        annualRatePercent = rate,
        durationMonths = months,
        initialFeePercent = feePct,
        earlySettlementMonth = settlementMonth,
        penaltyPercent = penaltyPct
    )

    val maxLoanReachable = if (isReverseMode) {
        FinancialFormulas.calculateMaxLoanFromPayment(
            desiredPayment = desiredPmt,
            annualRatePercent = rate,
            durationMonths = months
        )
    } else 0.0

    val copySummaryText = if (!isReverseMode) {
        """
            گزارش محاسبه وام و اقساط:
            مبلغ وام: ${PersianNumberUtils.formatCurrency(loanAmt, isRial = isRial)}
            نرخ سود سالانه: ${PersianNumberUtils.formatPercent(rate)} | مدت: ${PersianNumberUtils.toPersianDigits(durationMonthsInput)} ماه
            مبلغ قسط ماهانه: ${PersianNumberUtils.formatCurrency(loanResult.monthlyPayment, isRial = isRial)}
            کل بازپرداخت: ${PersianNumberUtils.formatCurrency(loanResult.totalRepayment, isRial = isRial)}
            کل سود پرداختی: ${PersianNumberUtils.formatCurrency(loanResult.totalInterest, isRial = isRial)}
            کارمزد اولیه: ${PersianNumberUtils.formatCurrency(loanResult.initialFeeAmount, isRial = isRial)}
        """.trimIndent()
    } else {
        """
            گزارش حالت معکوس وام (محاسبه سقف وام):
            مبلغ قسط پرداختی دلخواه: ${PersianNumberUtils.formatCurrency(desiredPmt, isRial = isRial)}
            نرخ سود: ${PersianNumberUtils.formatPercent(rate)} | مدت: ${PersianNumberUtils.toPersianDigits(durationMonthsInput)} ماه
            حداکثر مبلغ وام قابل دریافت: ${PersianNumberUtils.formatCurrency(maxLoanReachable, isRial = isRial)}
        """.trimIndent()
    }

    val detailsList = if (!isReverseMode) {
        listOf(
            "مبلغ وام" to PersianNumberUtils.formatCurrency(loanAmt, isRial = isRial),
            "نرخ سود سالانه" to PersianNumberUtils.formatPercent(rate),
            "مدت بازپرداخت" to "${PersianNumberUtils.toPersianDigits(durationMonthsInput)} ماه",
            "مبلغ قسط ماهانه" to PersianNumberUtils.formatCurrency(loanResult.monthlyPayment, isRial = isRial),
            "کل بازپرداخت" to PersianNumberUtils.formatCurrency(loanResult.totalRepayment, isRial = isRial),
            "کل سود وام" to PersianNumberUtils.formatCurrency(loanResult.totalInterest, isRial = isRial),
            "کارمزد اولیه" to PersianNumberUtils.formatCurrency(loanResult.initialFeeAmount, isRial = isRial)
        )
    } else {
        listOf(
            "مبلغ قسط دلخواه ماهانه" to PersianNumberUtils.formatCurrency(desiredPmt, isRial = isRial),
            "نرخ سود سالانه" to PersianNumberUtils.formatPercent(rate),
            "مدت" to "${PersianNumberUtils.toPersianDigits(durationMonthsInput)} ماه",
            "حداکثر سقف وام قابل دریافت" to PersianNumberUtils.formatCurrency(maxLoanReachable, isRial = isRial)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode switch
        item {
            NotebookCard {
                Text(
                    text = "حالت محاسبه وام",
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
                        onClick = { isReverseMode = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isReverseMode) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "محاسبه قسط",
                            color = if (!isReverseMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { isReverseMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReverseMode) AccentGold else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "حالت معکوس (سقف وام)",
                            color = if (isReverseMode) Color.Black else MaterialTheme.colorScheme.onSurface,
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
                if (!isReverseMode) {
                    PersianNumberTextField(
                        value = loanAmountInput,
                        onValueChange = { loanAmountInput = it },
                        label = "مبلغ اصل وام ($unitLabel)",
                        suffix = unitLabel
                    )
                } else {
                    PersianNumberTextField(
                        value = desiredPaymentInput,
                        onValueChange = { desiredPaymentInput = it },
                        label = "مبلغ قسط ماهانه دلخواه ($unitLabel)",
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
                        value = durationMonthsInput,
                        onValueChange = { durationMonthsInput = it },
                        label = "مدت بازپرداخت (ماه)",
                        suffix = "ماه",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Initial Fee & Early Settlement
                if (!isReverseMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PersianNumberTextField(
                            value = initialFeePercentInput,
                            onValueChange = { initialFeePercentInput = it },
                            label = "کارمزد اولیه (٪)",
                            suffix = "٪",
                            modifier = Modifier.weight(1f),
                            isDecimalAllowed = true
                        )

                        PersianNumberTextField(
                            value = earlySettlementMonthInput,
                            onValueChange = { earlySettlementMonthInput = it },
                            label = "ماه تسویه پیش از موعد (0=غیرفعال)",
                            suffix = "ماه",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (settlementMonth > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PersianNumberTextField(
                            value = penaltyPercentInput,
                            onValueChange = { penaltyPercentInput = it },
                            label = "جریمه/کارمزد تسویه زودهنگام (٪)",
                            suffix = "٪",
                            isDecimalAllowed = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val title = if (!isReverseMode) "وام - قسط ${PersianNumberUtils.formatCurrency(loanResult.monthlyPayment)}" else "وام معکوس - سقف ${PersianNumberUtils.formatCurrency(maxLoanReachable)}"
                        val summary = if (!isReverseMode) "وام: ${PersianNumberUtils.formatCurrency(loanAmt)}" else "قسط: ${PersianNumberUtils.formatCurrency(desiredPmt)}"
                        val params = "$isReverseMode|$loanAmountInput|$desiredPaymentInput|$rateInput|$durationMonthsInput|$initialFeePercentInput|$earlySettlementMonthInput|$penaltyPercentInput"
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "loan",
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
            if (!isReverseMode) {
                ResultHeaderBanner(
                    title = "نتیجه محاسبات قسط وام",
                    mainResultValue = PersianNumberUtils.formatCurrency(loanResult.monthlyPayment),
                    mainResultLabel = "مبلغ قسط پرداختی در هر ماه",
                    secondaryItems = listOf(
                        "کل بازپرداخت" to PersianNumberUtils.formatCurrency(loanResult.totalRepayment),
                        "کل سود وام" to PersianNumberUtils.formatCurrency(loanResult.totalInterest),
                        "کارمزد اولیه" to PersianNumberUtils.formatCurrency(loanResult.initialFeeAmount),
                        if (settlementMonth > 0) "صرفه‌جویی سود در تسویه زودهنگام" to PersianNumberUtils.formatCurrency(loanResult.totalInterestSaved) else "" to ""
                    ).filter { it.first.isNotEmpty() },
                    copySummaryText = copySummaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            } else {
                ResultHeaderBanner(
                    title = "حداکثر وام قابل دریافت",
                    mainResultValue = PersianNumberUtils.formatCurrency(maxLoanReachable),
                    mainResultLabel = "سقف وام با قسط ماهانه ${PersianNumberUtils.formatCurrency(desiredPmt)}",
                    secondaryItems = listOf(
                        "نرخ سود" to PersianNumberUtils.formatPercent(rate),
                        "مدت بازپرداخت" to "${PersianNumberUtils.toPersianDigits(durationMonthsInput)} ماه"
                    ),
                    copySummaryText = copySummaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            }
        }

        // Schedule Table
        if (!isReverseMode && loanResult.schedule.isNotEmpty()) {
            item {
                NotebookCard {
                    Text(
                        text = "جدول استهلاک وام",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    loanResult.schedule.take(12).forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ماه ${PersianNumberUtils.toPersianDigits(row.month.toString())}", fontWeight = FontWeight.Bold)
                            Text("اصل: ${PersianNumberUtils.formatCurrency(row.principalPart, showSuffix = false)}", style = MaterialTheme.typography.bodySmall)
                            Text("سود: ${PersianNumberUtils.formatCurrency(row.interestPart, showSuffix = false)}", style = MaterialTheme.typography.bodySmall)
                            Text("مانده: ${PersianNumberUtils.formatCurrency(row.remainingBalance, showSuffix = false)}", style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    if (loanResult.schedule.size > 12) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "... و ${PersianNumberUtils.toPersianDigits((loanResult.schedule.size - 12).toString())} ماه دیگر (کامل در خروجی PDF/اشتراک)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold
                        )
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
                        isReverseMode = parts[0].toBooleanStrictOrNull() ?: false
                        loanAmountInput = parts[1]
                        desiredPaymentInput = parts[2]
                        rateInput = parts[3]
                        durationMonthsInput = parts[4]
                        initialFeePercentInput = parts[5]
                        earlySettlementMonthInput = parts[6]
                        penaltyPercentInput = parts[7]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "محاسبه وام و اقساط",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
