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
import androidx.compose.material3.Slider
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
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.HoldingSummary
import com.example.ui.components.HistoryAccordion
import com.example.ui.components.NotebookCard
import com.example.ui.components.PrivacyAwareAmountText
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.util.GoldMarketFormulas
import com.example.util.PersianNumberUtils
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import kotlin.math.roundToInt

private fun PortfolioAssetType.displayName(): String = when (this) {
    PortfolioAssetType.GOLD -> "طلا"
    PortfolioAssetType.USD -> "دلار"
    PortfolioAssetType.STOCK -> "سهام"
    PortfolioAssetType.CASH -> "نقد"
    PortfolioAssetType.CRYPTO -> "کریپتو"
}

/**
 * شبیه‌ساز سناریو — "اگر دلار بشود … ارزش سبد من چقدر می‌شود؟"
 * Legs start from the REAL current portfolio values (grouped by asset type); the user drags
 * a what-if price change per leg and the whole portfolio is re-valued live.
 */
@Composable
fun ScenarioScreen(
    holdings: List<HoldingSummary> = emptyList(),
    historyList: List<CalculationHistoryEntity>,
    onAddHistory: (CalculationHistoryEntity) -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val portfolioLegs = remember(holdings) {
        PortfolioAssetType.entries.mapNotNull { type ->
            val value = holdings.filter { it.assetType == type }.sumOf { it.currentValueRial }
            if (value > 0) Triple(type.displayName(), value, type) else null
        }
    }

    var manualMode by remember { mutableStateOf(portfolioLegs.isEmpty()) }
    var goldManual by remember { mutableStateOf("100000000") }
    var usdManual by remember { mutableStateOf("50000000") }
    var stockManual by remember { mutableStateOf("50000000") }

    var goldPct by remember { mutableStateOf(0f) }
    var usdPct by remember { mutableStateOf(0f) }
    var stockPct by remember { mutableStateOf(0f) }

    val legs: List<Triple<String, Double, Double>> = if (manualMode) {
        listOf(
            Triple("طلا", PersianNumberUtils.parseAmount(goldManual), goldPct.toDouble()),
            Triple("دلار", PersianNumberUtils.parseAmount(usdManual), usdPct.toDouble()),
            Triple("سهام", PersianNumberUtils.parseAmount(stockManual), stockPct.toDouble())
        )
    } else {
        portfolioLegs.map { (name, value, type) ->
            val pct = when (type) {
                PortfolioAssetType.GOLD -> goldPct
                PortfolioAssetType.USD -> usdPct
                PortfolioAssetType.STOCK -> stockPct
                PortfolioAssetType.CRYPTO -> 0f // Or add a cryptoPct slider if needed later
                PortfolioAssetType.CASH -> 0f // Cash doesn't usually change price in a "what-if" scenario relative to itself
            }
            Triple(name, value, pct.toDouble())
        }
    }

    val result = GoldMarketFormulas.calculateScenario(legs)

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NotebookCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "اگر بازار این‌طور شود…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    if (portfolioLegs.isNotEmpty()) {
                        Button(
                            onClick = { manualMode = !manualMode },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                if (manualMode) "استفاده از سبد واقعی" else "ورود دستی مبالغ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (!manualMode && portfolioLegs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "مبالغ از دارایی‌های فعلی سبدت خوانده شده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (manualMode) {
            item {
                NotebookCard {
                    ManualValueField("ارزش فعلی طلا (ریال)", goldManual, { goldManual = it })
                    Spacer(modifier = Modifier.height(10.dp))
                    ManualValueField("ارزش فعلی دلار (ریال)", usdManual, { usdManual = it })
                    Spacer(modifier = Modifier.height(10.dp))
                    ManualValueField("ارزش فعلی سهام (ریال)", stockManual, { stockManual = it })
                }
            }
        }

        item {
            NotebookCard {
                ScenarioSlider("طلا", goldPct, { goldPct = it })
                Spacer(modifier = Modifier.height(12.dp))
                ScenarioSlider("دلار", usdPct, { usdPct = it })
                Spacer(modifier = Modifier.height(12.dp))
                ScenarioSlider("سهام", stockPct, { stockPct = it })
            }
        }

        if (result != null) {
            item {
                NotebookCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ارزش فعلی سبد", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        PrivacyAwareAmountText(text = formatRial(result.currentValue))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ارزش سبد در این سناریو", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        PrivacyAwareAmountText(
                            text = formatRial(result.simulatedValue),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val positive = result.changeAmount >= 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تغییر", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        PrivacyAwareAmountText(
                            text = "${formatRial(result.changeAmount)} (${formatPercentSigned(result.changePercent)})",
                            color = if (positive) ProfitGreen else LossRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onAddHistory(
                                CalculationHistoryEntity(
                                    sectionKey = "scenario",
                                    title = "سناریو: طلا ${goldPct.roundToInt()}٪ / دلار ${usdPct.roundToInt()}٪ / سهام ${stockPct.roundToInt()}٪",
                                    summary = "ارزش سبد: ${formatRial(result.currentValue, showSuffix = false)} → ${formatRial(result.simulatedValue, showSuffix = false)} ریال (${formatPercentSigned(result.changePercent)})",
                                    paramsJson = "$manualMode|$goldManual|$usdManual|$stockManual|$goldPct|$usdPct|$stockPct"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Text("ذخیره در تاریخچه", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            item {
                NotebookCard {
                    Text(
                        "برای شبیه‌سازی، ارزش فعلی حداقل یک دارایی را بزرگ‌تر از صفر وارد کن.",
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
                    if (parts.size >= 7) {
                        manualMode = parts[0].toBooleanStrictOrNull() ?: manualMode
                        goldManual = parts[1]
                        usdManual = parts[2]
                        stockManual = parts[3]
                        goldPct = parts[4].toFloatOrNull() ?: goldPct
                        usdPct = parts[5].toFloatOrNull() ?: usdPct
                        stockPct = parts[6].toFloatOrNull() ?: stockPct
                    }
                },
                onDeleteHistory = onDeleteHistory,
                onClearAll = onClearHistory
            )
        }
    }
}

@Composable
private fun ScenarioSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(
                formatPercentSigned(value.toDouble()),
                color = if (value >= 0) ProfitGreen else LossRed,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = -50f..100f,
            steps = 29
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("۵۰-٪", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("۱۰۰+٪", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ManualValueField(label: String, value: String, onChange: (String) -> Unit) {
    com.example.ui.components.PersianNumberTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        suffix = "ریال"
    )
}
