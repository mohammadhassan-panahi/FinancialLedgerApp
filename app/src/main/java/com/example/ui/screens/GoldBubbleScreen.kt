package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.ui.theme.DaraTypography
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.LossRed
import com.example.ui.theme.ObsidianSlate900
import com.example.ui.theme.Slate50
import com.example.util.GoldMarketFormulas
import com.example.util.GoldMarketFormulas.GoldItemType
import com.example.util.PersianNumberUtils

@Composable
fun GoldBubbleScreen(
    historyList: List<CalculationHistoryEntity>,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var itemType by remember { mutableStateOf(GoldItemType.EMAAMI) }
    var ounceInput by remember { mutableStateOf("2500") }
    var dollarInput by remember { mutableStateOf("60000") }
    var marketInput by remember { mutableStateOf("45000000") }
    var showPrintDialog by remember { mutableStateOf(false) }

    val ounce = PersianNumberUtils.parseAmount(ounceInput)
    val dollar = PersianNumberUtils.parseAmount(dollarInput)
    val market = PersianNumberUtils.parseAmount(marketInput)
    val result = GoldMarketFormulas.calculateGoldBubble(itemType, ounce, dollar, market)

    val summaryText = result?.let {
        """
            گزارش حباب ${itemType.displayName}:
            انس جهانی: ${PersianNumberUtils.toPersianDigits(ounceInput)} دلار | دلار: ${PersianNumberUtils.formatCurrency(dollar, showSuffix = false)} تومان
            ارزش ذاتی: ${PersianNumberUtils.formatCurrency(it.intrinsicValueToman, showSuffix = false)} تومان
            قیمت بازار: ${PersianNumberUtils.formatCurrency(market, showSuffix = false)} تومان
            حباب: ${PersianNumberUtils.formatCurrency(it.bubbleAmountToman, showSuffix = false)} تومان (${PersianNumberUtils.formatPercent(it.bubblePercent)})
            ${it.verdict}
        """.trimIndent()
    } ?: ""

    val detailsList = result?.let {
        listOf(
            "ارزش ذاتی" to PersianNumberUtils.formatCurrency(it.intrinsicValueToman, showSuffix = false),
            "قیمت بازار" to PersianNumberUtils.formatCurrency(market, showSuffix = false),
            "حباب مبلغی" to PersianNumberUtils.formatCurrency(it.bubbleAmountToman, showSuffix = false),
            "حباب درصدی" to PersianNumberUtils.formatPercent(it.bubblePercent),
            "طلای خالص" to "${PersianNumberUtils.toPersianDigits(itemType.pureGoldGrams.toString())} گرم",
            "جمع‌بندی" to it.verdict
        )
    } ?: emptyList()

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                color = ObsidianSlate900.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.height(64.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate50)
                        }
                        Column {
                            Text("حباب طلا و سکه", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                            Text("تحلیل ارزش ذاتی و حباب", style = DaraTypography.labelSmall, color = IndigoElectric)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                NotebookCard {
                    Text(
                        "انتخاب نوع دارایی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    GoldItemType.entries.forEach { type ->
                        FilterChip(
                            selected = itemType == type,
                            onClick = { itemType = type },
                            label = { Text(type.displayName, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        )
                    }
                }
            }

            item {
                NotebookCard {
                    PersianNumberTextField(
                        value = ounceInput,
                        onValueChange = { ounceInput = it },
                        label = "انس جهانی (دلار)",
                        suffix = "دلار",
                        isDecimalAllowed = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PersianNumberTextField(
                        value = dollarInput,
                        onValueChange = { dollarInput = it },
                        label = "قیمت دلار (تومان)",
                        suffix = "تومان"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PersianNumberTextField(
                        value = marketInput,
                        onValueChange = { marketInput = it },
                        label = "قیمت بازار (تومان)",
                        suffix = "تومان"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            result ?: return@Button
                            onAddHistory(
                                CalculationHistoryEntity(
                                    sectionKey = "gold_bubble",
                                    title = "حباب ${itemType.displayName}",
                                    summary = "حباب: ${PersianNumberUtils.formatCurrency(result.bubbleAmountToman, showSuffix = false)}",
                                    paramsJson = "$itemType|$ounceInput|$dollarInput|$marketInput"
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
                        title = "حباب ${itemType.displayName}",
                        mainResultValue = PersianNumberUtils.formatCurrency(result.bubbleAmountToman, showSuffix = false),
                        mainResultLabel = "حباب قیمت: ${PersianNumberUtils.formatPercent(result.bubblePercent)}",
                        secondaryItems = listOf(
                            "ارزش ذاتی" to PersianNumberUtils.formatCurrency(result.intrinsicValueToman, showSuffix = false),
                            "قیمت بازار" to PersianNumberUtils.formatCurrency(market, showSuffix = false)
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
                        if (parts.size >= 4) {
                            itemType = runCatching { GoldItemType.valueOf(parts[0]) }.getOrDefault(GoldItemType.EMAAMI)
                            ounceInput = parts[1]
                            dollarInput = parts[2]
                            marketInput = parts[3]
                        }
                    },
                    onDeleteHistory = onDeleteHistory,
                    onClearAll = onClearHistory
                )
            }
        }
    }

    if (showPrintDialog && result != null) {
        PrintPdfDialog(
            sectionTitle = "حباب طلا و سکه",
            summaryContent = summaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
