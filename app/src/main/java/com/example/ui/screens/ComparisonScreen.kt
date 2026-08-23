package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.components.ComposeLineChart
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.LineChartSeries
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.components.PrintPdfDialog
import com.example.ui.components.ResultHeaderBanner
import com.example.ui.components.chartColors
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.util.FinancialFormulas
import com.example.util.PersianNumberUtils

@Composable
fun ComparisonScreen(
    historyList: List<CalculationHistoryEntity>,
    defaultInflation: Double,
    currencyUnit: String = "تومان",
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val isRial = currencyUnit == "ریال"
    val unitLabel = PersianNumberUtils.getCurrencyUnitLabel(isRial)

    val scenarios = remember {
        mutableStateListOf(
            FinancialFormulas.ComparisonScenarioInput("صندوق درآمد ثابت", 100000000.0, 5000000.0, 24.0, 5),
            FinancialFormulas.ComparisonScenarioInput("طلا و سکه", 100000000.0, 5000000.0, 45.0, 5)
        )
    }

    var includeInflationEffect by remember { mutableStateOf(true) }
    var inflationRateInput by remember { mutableStateOf(defaultInflation.toString()) }
    var showPrintDialog by remember { mutableStateOf(false) }

    val inflation = inflationRateInput.toDoubleOrNull() ?: 0.0

    val results = FinancialFormulas.compareScenarios(scenarios, inflation)

    val copySummaryText = buildString {
        append("گزارش مقایسه گزینه‌های سرمایه‌گذاری:\n")
        results.forEach { res ->
            append("• ${res.name}: ارزش نهایی اسمی = ${PersianNumberUtils.formatCurrency(res.finalNominalValue, isRial = isRial)} | ارزش واقعی = ${PersianNumberUtils.formatCurrency(res.finalRealValue, isRial = isRial)}\n")
        }
    }

    val detailsList = results.flatMap { res ->
        listOf(
            "سناریو: ${res.name}" to "ارزش نهایی: ${PersianNumberUtils.formatCurrency(res.finalNominalValue, isRial = isRial)}",
            "ارزش واقعی (${res.name})" to PersianNumberUtils.formatCurrency(res.finalRealValue, isRial = isRial)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle Inflation & Config
        item {
            NotebookCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لحاظ اثر تورم در مقایسه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                        Text(
                            text = "نمایش خط نقطه چین ارزش واقعی خرید",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Switch(
                        checked = includeInflationEffect,
                        onCheckedChange = { includeInflationEffect = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGold)
                    )
                }

                if (includeInflationEffect) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PersianNumberTextField(
                        value = inflationRateInput,
                        onValueChange = { inflationRateInput = it },
                        label = "نرخ تورم سالانه (٪)",
                        suffix = "٪",
                        isDecimalAllowed = true
                    )
                }
            }
        }

        // Scenarios Form List
        item {
            NotebookCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سناریوهای سرمایه‌گذاری (۲ تا ۴ گزینه)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )

                    if (scenarios.size < 4) {
                        Button(
                            onClick = {
                                scenarios.add(
                                    FinancialFormulas.ComparisonScenarioInput(
                                        "گزینه جدید ${PersianNumberUtils.toPersianDigits((scenarios.size + 1).toString())}",
                                        100000000.0, 2000000.0, 30.0, 5
                                    )
                                )
                            },
                            modifier = Modifier.height(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "افزودن سناریو",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                scenarios.forEachIndexed { idx, sc ->
                    var nameState by remember(sc.name) { mutableStateOf(sc.name) }
                    var initAmtState by remember(sc.initialAmount) { mutableStateOf(sc.initialAmount.toLong().toString()) }
                    var monthDepState by remember(sc.monthlyDeposit) { mutableStateOf(sc.monthlyDeposit.toLong().toString()) }
                    var rateState by remember(sc.annualRatePercent) { mutableStateOf(sc.annualRatePercent.toString()) }
                    var yearsState by remember(sc.durationYears) { mutableStateOf(sc.durationYears.toString()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = nameState,
                                    onValueChange = {
                                        nameState = it
                                        scenarios[idx] = sc.copy(name = it)
                                    },
                                    label = { Text("نام گزینه") },
                                    modifier = Modifier.weight(1f)
                                )

                                if (scenarios.size > 2) {
                                    IconButton(onClick = { scenarios.removeAt(idx) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = LossRed)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PersianNumberTextField(
                                    value = initAmtState,
                                    onValueChange = {
                                        initAmtState = it
                                        val v = PersianNumberUtils.parseAmountToToman(it, isRial)
                                        scenarios[idx] = scenarios[idx].copy(initialAmount = v)
                                    },
                                    label = "مبلغ اولیه",
                                    suffix = unitLabel,
                                    modifier = Modifier.weight(1f)
                                )

                                PersianNumberTextField(
                                    value = monthDepState,
                                    onValueChange = {
                                        monthDepState = it
                                        val v = PersianNumberUtils.parseAmountToToman(it, isRial)
                                        scenarios[idx] = scenarios[idx].copy(monthlyDeposit = v)
                                    },
                                    label = "واریز ماهانه",
                                    suffix = unitLabel,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PersianNumberTextField(
                                    value = rateState,
                                    onValueChange = {
                                        rateState = it
                                        val v = it.toDoubleOrNull() ?: 0.0
                                        scenarios[idx] = scenarios[idx].copy(annualRatePercent = v)
                                    },
                                    label = "نرخ سود سالانه (٪)",
                                    suffix = "٪",
                                    modifier = Modifier.weight(1f),
                                    isDecimalAllowed = true
                                )

                                PersianNumberTextField(
                                    value = yearsState,
                                    onValueChange = {
                                        yearsState = it
                                        val v = it.toIntOrNull() ?: 1
                                        scenarios[idx] = scenarios[idx].copy(durationYears = v)
                                    },
                                    label = "مدت (سال)",
                                    suffix = "سال",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val title = "مقایسه ${PersianNumberUtils.toPersianDigits(scenarios.size.toString())} گزینه"
                        val best = results.maxByOrNull { it.finalNominalValue }
                        val summary = "بهترین: ${best?.name} (${PersianNumberUtils.formatCurrency(best?.finalNominalValue ?: 0.0)})"
                        val params = scenarios.joinToString(";") { "${it.name},${it.initialAmount},${it.monthlyDeposit},${it.annualRatePercent},${it.durationYears}" }
                        onAddHistory(
                            CalculationHistoryEntity(
                                sectionKey = "comparison",
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

        // Comparative Chart
        item {
            NotebookCard {
                Text(
                    text = "نمودار مقایسه‌ای روند رشد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val lineSeriesList = mutableListOf<LineChartSeries>()

                results.forEachIndexed { idx, res ->
                    val color = chartColors[idx % chartColors.size]
                    val nominalPoints = res.yearPoints.map { Pair(it.year.toDouble(), it.nominalValue) }
                    lineSeriesList.add(LineChartSeries(res.name, nominalPoints, color))

                    if (includeInflationEffect) {
                        val realPoints = res.yearPoints.map { Pair(it.year.toDouble(), it.realValue) }
                        lineSeriesList.add(LineChartSeries("${res.name} (واقعی)", realPoints, color, isDotted = true))
                    }
                }

                ComposeLineChart(
                    seriesList = lineSeriesList,
                    xAxisLabel = "سال"
                )
            }
        }

        // Summary Comparison Cards
        item {
            val best = results.maxByOrNull { it.finalNominalValue }
            ResultHeaderBanner(
                title = "نتیجه مقایسه گزینه‌ها",
                mainResultValue = PersianNumberUtils.formatCurrency(best?.finalNominalValue ?: 0.0, isRial = isRial),
                mainResultLabel = "بهترین عملکرد مربوط به گزینه «${best?.name}»",
                secondaryItems = results.map { res ->
                    res.name to PersianNumberUtils.formatCurrency(res.finalNominalValue, isRial = isRial)
                },
                copySummaryText = copySummaryText,
                onPrintClick = { showPrintDialog = true }
            )
        }

        // History Accordion
        item {
            HistoryAccordion(
                historyList = historyList,
                onSelectHistory = { hist ->
                    val scenarioBlocks = hist.paramsJson.split(";")
                    if (scenarioBlocks.isNotEmpty()) {
                        scenarios.clear()
                        scenarioBlocks.forEach { block ->
                            val parts = block.split(",")
                            if (parts.size >= 5) {
                                scenarios.add(
                                    FinancialFormulas.ComparisonScenarioInput(
                                        name = parts[0],
                                        initialAmount = parts[1].toDoubleOrNull() ?: 100000000.0,
                                        monthlyDeposit = parts[2].toDoubleOrNull() ?: 0.0,
                                        annualRatePercent = parts[3].toDoubleOrNull() ?: 20.0,
                                        durationYears = parts[4].toIntOrNull() ?: 5
                                    )
                                )
                            }
                        }
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }

    if (showPrintDialog) {
        PrintPdfDialog(
            sectionTitle = "مقایسه سرمایه‌گذاری‌ها",
            summaryContent = copySummaryText,
            detailsList = detailsList,
            onDismiss = { showPrintDialog = false }
        )
    }
}
