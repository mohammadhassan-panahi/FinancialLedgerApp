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
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationHistoryEntity
import com.example.ui.components.HistoryAccordion
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
fun GoldFxScreen(
    historyList: List<CalculationHistoryEntity>,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val isRial = currencyUnit == "ریال"
    val unitLabel = PersianNumberUtils.getCurrencyUnitLabel(isRial)

    var isReverseMode by remember { mutableStateOf(false) }

    var assetNameInput by remember { mutableStateOf("طلا ۱۸ عیار") }
    var buyPriceInput by remember { mutableStateOf(if (isRial) "43500000" else "4350000") }
    var sellPriceInput by remember { mutableStateOf(if (isRial) "48000000" else "4800000") }
    var quantityInput by remember { mutableStateOf("10") }
    var targetProfitPercentInput by remember { mutableStateOf("15") }

    var showPrintDialog by remember { mutableStateOf(false) }

    val buyP = PersianNumberUtils.parseAmountToToman(buyPriceInput, isRial)
    val sellP = PersianNumberUtils.parseAmountToToman(sellPriceInput, isRial)
    val qty = quantityInput.toDoubleOrNull() ?: 1.0
    val targetPct = targetProfitPercentInput.toDoubleOrNull() ?: 0.0

    val tradeResult = FinancialFormulas.calculateTradeProfit(
        assetName = assetNameInput,
        buyPrice = buyP,
        sellPrice = sellP,
        quantity = qty
    )

    val requiredSellP = if (isReverseMode) {
        FinancialFormulas.calculateRequiredSellingPrice(buyP, targetPct)
    } else 0.0

    val copySummaryText = if (!isReverseMode) {
        """
            گزارش سود/زیان طلا و ارز:
            دارایی: $assetNameInput
            مقدار: ${PersianNumberUtils.toPersianDigits(quantityInput)} | قیمت خرید: ${PersianNumberUtils.formatCurrency(buyP, isRial = isRial)} | قیمت فروش: ${PersianNumberUtils.formatCurrency(sellP, isRial = isRial)}
            کل ارزش خرید: ${PersianNumberUtils.formatCurrency(tradeResult.totalBuyValue, isRial = isRial)}
            کل ارزش فروش: ${PersianNumberUtils.formatCurrency(tradeResult.totalSellValue, isRial = isRial)}
            سود/زیان کل: ${PersianNumberUtils.formatCurrency(tradeResult.profitLossAmount, isRial = isRial)} (${PersianNumberUtils.formatPercent(tradeResult.profitLossPercentage)})
        """.trimIndent()
    } else {
        """
            گزارش قیمت فروش هدف طلا و ارز:
            دارایی: $assetNameInput | قیمت خرید: ${PersianNumberUtils.formatCurrency(buyP, isRial = isRial)}
            درصد سود دلخواه: ${PersianNumberUtils.formatPercent(targetPct)}
            قیمت فروش لازم: ${PersianNumberUtils.formatCurrency(requiredSellP, isRial = isRial)}
        """.trimIndent()
    }

    val detailsList = if (!isReverseMode) {
        listOf(
            "نام دارایی" to assetNameInput,
            "مقدار" to PersianNumberUtils.toPersianDigits(quantityInput),
            "قیمت خرید واحد" to PersianNumberUtils.formatCurrency(buyP, isRial = isRial),
            "قیمت فروش واحد" to PersianNumberUtils.formatCurrency(sellP, isRial = isRial),
            "مجموع ارزش خرید" to PersianNumberUtils.formatCurrency(tradeResult.totalBuyValue, isRial = isRial),
            "مجموع ارزش فروش" to PersianNumberUtils.formatCurrency(tradeResult.totalSellValue, isRial = isRial),
            "سود/زیان مبلغی" to PersianNumberUtils.formatCurrency(tradeResult.profitLossAmount, isRial = isRial),
            "سود/زیان درصدی" to PersianNumberUtils.formatPercent(tradeResult.profitLossPercentage)
        )
    } else {
        listOf(
            "نام دارایی" to assetNameInput,
            "قیمت خرید واحد" to PersianNumberUtils.formatCurrency(buyP, isRial = isRial),
            "درصد سود مورد نظر" to PersianNumberUtils.formatPercent(targetPct),
            "قیمت فروش لازم برای تحقق سود" to PersianNumberUtils.formatCurrency(requiredSellP, isRial = isRial)
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
                    text = "حالت محاسبه معامله طلا و ارز",
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
                            text = "محاسبه سود فعلی",
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
                            text = "حالت معکوس (قیمت فروش هدف)",
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

        // Form
        item {
            NotebookCard {
                OutlinedTextField(
                    value = assetNameInput,
                    onValueChange = { assetNameInput = it },
                    label = { Text("نام دارایی / ارز (مثال: سکه امامی / دلار / طلا ۱۸)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                PersianNumberTextField(
                    value = buyPriceInput,
                    onValueChange = { buyPriceInput = it },
                    label = "قیمت خرید واحد ($unitLabel)",
                    suffix = unitLabel
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (!isReverseMode) {
                    PersianNumberTextField(
                        value = sellPriceInput,
                        onValueChange = { sellPriceInput = it },
                        label = "قیمت فروش واحد ($unitLabel)",
                        suffix = unitLabel
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PersianNumberTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = "مقدار / تعداد",
                        isDecimalAllowed = true
                    )
                } else {
                    PersianNumberTextField(
                        value = targetProfitPercentInput,
                        onValueChange = { targetProfitPercentInput = it },
                        label = "درصد سود دلخواه (٪)",
                        suffix = "٪",
                        isDecimalAllowed = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val title = "$assetNameInput - ${if (!isReverseMode) "سود/زیان" else "قیمت هدف"}"
                        val summary = if (!isReverseMode) "سود: ${PersianNumberUtils.formatCurrency(tradeResult.profitLossAmount)}" else "فروش لازم: ${PersianNumberUtils.formatCurrency(requiredSellP)}"
                        val params = "$isReverseMode|$assetNameInput|$buyPriceInput|$sellPriceInput|$quantityInput|$targetProfitPercentInput"
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "gold_fx",
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
                    title = "نتیجه معامله $assetNameInput",
                    mainResultValue = PersianNumberUtils.formatCurrency(tradeResult.profitLossAmount),
                    mainResultLabel = "سود/زیان کل معامله (${PersianNumberUtils.formatPercent(tradeResult.profitLossPercentage)})",
                    secondaryItems = listOf(
                        "مجموع خرید" to PersianNumberUtils.formatCurrency(tradeResult.totalBuyValue),
                        "مجموع فروش" to PersianNumberUtils.formatCurrency(tradeResult.totalSellValue)
                    ),
                    copySummaryText = copySummaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            } else {
                ResultHeaderBanner(
                    title = "قیمت فروش هدف جهت تحقق ${PersianNumberUtils.formatPercent(targetPct)} سود",
                    mainResultValue = PersianNumberUtils.formatCurrency(requiredSellP),
                    mainResultLabel = "باید هر واحد را به این قیمت بفروشید",
                    secondaryItems = listOf(
                        "قیمت خرید واحد" to PersianNumberUtils.formatCurrency(buyP),
                        "سود مورد نظر" to PersianNumberUtils.formatPercent(targetPct)
                    ),
                    copySummaryText = copySummaryText,
                    onPrintClick = { showPrintDialog = true }
                )
            }
        }

        // History Accordion
        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val parts = hist.paramsJson.split("|")
                    if (parts.size >= 6) {
                        isReverseMode = parts[0].toBooleanStrictOrNull() ?: false
                        assetNameInput = parts[1]
                        buyPriceInput = parts[2]
                        sellPriceInput = parts[3]
                        quantityInput = parts[4]
                        targetProfitPercentInput = parts[5]
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "سود طلا، دلار و ارز",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
