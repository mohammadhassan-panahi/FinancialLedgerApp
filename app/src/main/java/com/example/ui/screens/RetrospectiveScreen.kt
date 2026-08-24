package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.components.PrintPdfDialog
import com.example.ui.components.ResultHeaderBanner
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.util.GoldMarketFormulas
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberUtils
import java.util.Date

/**
 * ماشین‌حساب گذشته‌نگر — "if I had spent X when the price was Y, what would I own today?"
 * The asset and dates are free-form labels so it works for any market (gold, USD, crypto, …).
 */
@Composable
fun RetrospectiveScreen(
    historyList: List<CalculationHistoryEntity>,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    var assetNameInput by remember { mutableStateOf("طلای ۱۸ عیار") }
    var amountInput by remember { mutableStateOf("100000000") }
    var pastDateInput by remember { mutableStateOf(PersianDateUtils.formatJalaliDate(Date(Date().time - 365L * 24 * 3600 * 1000))) }
    var pastPriceInput by remember { mutableStateOf("35000000") }
    var currentPriceInput by remember { mutableStateOf("50000000") }
    var showPrintDialog by remember { mutableStateOf(false) }

    val amount = PersianNumberUtils.parseAmount(amountInput)
    val pastPrice = PersianNumberUtils.parseAmount(pastPriceInput)
    val currentPrice = PersianNumberUtils.parseAmount(currentPriceInput)
    val result = GoldMarketFormulas.calculateRetrospective(amount, pastPrice, currentPrice)

    val summaryText = result?.let {
        """
            گزارش سرمایه‌گذاری گذشته‌نگر:
            دارایی: $assetNameInput | تاریخ فرضی خرید: $pastDateInput
            سرمایه: ${PersianNumberUtils.formatCurrency(amount, showSuffix = false)} تومان
            قیمت آن روز: ${PersianNumberUtils.formatCurrency(pastPrice, showSuffix = false)} | قیمت امروز: ${PersianNumberUtils.formatCurrency(currentPrice, showSuffix = false)}
            مقدار خریداری‌شده: ${PersianNumberUtils.toPersianDigits("%.4f".format(result.quantity))}
            ارزش امروز: ${PersianNumberUtils.formatCurrency(it.currentValue, showSuffix = false)} تومان
            سود/زیان: ${PersianNumberUtils.formatCurrency(it.profitAmount, showSuffix = false)} تومان (${PersianNumberUtils.formatPercent(it.profitPercent)})
        """.trimIndent()
    } ?: ""

    val detailsList = result?.let {
        listOf(
            "دارایی" to assetNameInput,
            "تاریخ فرضی خرید" to pastDateInput,
            "سرمایه اولیه" to PersianNumberUtils.formatCurrency(amount, showSuffix = false),
            "مقدار خریداری‌شده" to PersianNumberUtils.toPersianDigits("%.4f".format(it.quantity)),
            "ارزش امروز" to PersianNumberUtils.formatCurrency(it.currentValue, showSuffix = false),
            "سود/زیان" to "${PersianNumberUtils.formatCurrency(it.profitAmount, showSuffix = false)} (${PersianNumberUtils.formatPercent(it.profitPercent)})"
        )
    } ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NotebookCard {
                OutlinedTextField(
                    value = assetNameInput,
                    onValueChange = { assetNameInput = it },
                    label = { Text("دارایی (مثال: دلار / بیت‌کوین / سکه امامی)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = "سرمایه‌ای که خرج می‌کردی (تومان)",
                    suffix = "تومان"
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = pastDateInput,
                    onValueChange = { pastDateInput = it },
                    label = { Text("تاریخ فرضی خرید (شمسی)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = pastPriceInput,
                    onValueChange = { pastPriceInput = it },
                    label = "قیمت آن روز (تومان)",
                    suffix = "تومان"
                )
                Spacer(modifier = Modifier.height(10.dp))
                PersianNumberTextField(
                    value = currentPriceInput,
                    onValueChange = { currentPriceInput = it },
                    label = "قیمت امروز (تومان)",
                    suffix = "تومان"
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        result ?: return@Button
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "retrospective",
                                title = "$assetNameInput - گذشته‌نگر",
                                summary = "سود فرضی: ${PersianNumberUtils.formatCurrency(result.profitAmount, showSuffix = false)} تومان (${PersianNumberUtils.formatPercent(result.profitPercent)})",
                                paramsJson = "$assetNameInput|$amountInput|$pastDateInput|$pastPriceInput|$currentPriceInput"
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
                    title = "اگر آن روز $assetNameInput می‌خریدی…",
                    mainResultValue = PersianNumberUtils.formatCurrency(result.currentValue, showSuffix = false),
                    mainResultLabel = "ارزش امروز سرمایه‌ات: ${PersianNumberUtils.formatCurrency(result.profitAmount, showSuffix = false)} تومان سود/زیان (${PersianNumberUtils.formatPercent(result.profitPercent)})",
                    secondaryItems = listOf(
                        "مقدار خریداری‌شده" to PersianNumberUtils.toPersianDigits("%.4f".format(result.quantity)),
                        "سرمایه اولیه" to PersianNumberUtils.formatCurrency(amount, showSuffix = false)
                    ),
                    copySummaryText = summaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            }
        } else {
            item {
                NotebookCard {
                    Text(
                        "سرمایه، قیمت آن روز و قیمت امروز را وارد کن (قیمت‌ها باید بزرگ‌تر از صفر باشند).",
                        color = LossRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 5) {
                        assetNameInput = parts[0]
                        amountInput = parts[1]
                        pastDateInput = parts[2]
                        pastPriceInput = parts[3]
                        currentPriceInput = parts[4]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog && result != null) {
        PrintPdfDialog(
            sectionTitle = "ماشین‌حساب گذشته‌نگر",
            summaryContent = summaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
