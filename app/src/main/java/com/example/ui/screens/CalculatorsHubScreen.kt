package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NotebookCard
import com.example.ui.theme.AccentGold
import com.example.ui.viewmodel.CalculatorViewModel

data class CalculatorItemInfo(
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

@Composable
fun CalculatorsHubScreen(
    viewModel: CalculatorViewModel,
    onBack: () -> Unit = {},
    currencyUnit: String = "تومان",
    defaultInflation: Double = 40.0,
    defaultTax: Double = 0.0
) {
    var activeCalculatorIndex by remember { mutableStateOf<Int?>(null) }

    val calculatorsList = listOf(
        CalculatorItemInfo(
            title = "۱. سود ساده",
            description = "محاسبه سود اصل سرمایه با نرخ ثابت سالانه بدون مرکب‌سازی",
            category = "پایه",
            icon = Icons.Default.Percent,
            iconBgColor = Color(0xFF2563EB)
        ),
        CalculatorItemInfo(
            title = "۲. سود مرکب",
            description = "محاسبه اثر مرکب‌سازی سود، سرمایه‌گذاری مجدد و رشد آتی",
            category = "رشد",
            icon = Icons.Default.TrendingUp,
            iconBgColor = Color(0xFF059669)
        ),
        CalculatorItemInfo(
            title = "۳. وام و اقساط",
            description = "محاسبه مبلغ قسط ماهانه، سود کل پرداختی و جدول اقساط",
            category = "تسهیلات",
            icon = Icons.Default.Calculate,
            iconBgColor = Color(0xFFD97706)
        ),
        CalculatorItemInfo(
            title = "۴. سپرده بانکی",
            description = "محاسبه سود روزشمار/ماهیانه سپرده بانکی و کسر تورم",
            category = "بانکی",
            icon = Icons.Default.AccountBalance,
            iconBgColor = Color(0xFF7C3AED)
        ),
        CalculatorItemInfo(
            title = "۵. تورم و قدرت خرید",
            description = "ارزیابی افت ارزش پول ملی و محاسبه قدرت خرید در سال‌های آینده",
            category = "اقتصادی",
            icon = Icons.Default.PriceChange,
            iconBgColor = Color(0xFFDC2626)
        ),
        CalculatorItemInfo(
            title = "۶. سود طلا و ارز",
            description = "محاسبه حباب سکه/طلا، سود معاملات و نوسانات ارز",
            category = "بازار",
            icon = Icons.Default.MonetizationOn,
            iconBgColor = Color(0xFFCA8A04)
        ),
        CalculatorItemInfo(
            title = "۷. مقایسه گزینه‌ها",
            description = "مقایسه همزمان و تحلیل بازدهی واقعی ۳ سناریوی مختلف",
            category = "تحلیلی",
            icon = Icons.Default.CompareArrows,
            iconBgColor = Color(0xFF0891B2)
        )
    )

    if (activeCalculatorIndex == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back navigation to Home
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "بازگشت",
                        tint = AccentGold
                    )
                }
                Text(
                    text = "بازگشت",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentGold,
                    modifier = Modifier.clickable { onBack() }
                )
            }

            // Header Banner
            NotebookCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مرکز ماشین‌حساب‌های مالی تخصصی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "جهت انجام محاسبات دقیق، یکی از ۷ ابزار هوشمند زیر را انتخاب کنید:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Grid of 7 Calculators
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(calculatorsList) { index, calc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeCalculatorIndex = index }
                            .testTag("calculator_hub_item_$index"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = calc.iconBgColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = calc.icon,
                                        contentDescription = calc.title,
                                        tint = calc.iconBgColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = calc.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = calc.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = calc.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Open",
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Active Sub-Calculator View
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Back Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeCalculatorIndex = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = AccentGold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بازگشت به فهرست ماشین‌حساب‌ها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentGold,
                        modifier = Modifier.clickable { activeCalculatorIndex = null }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = calculatorsList.getOrNull(activeCalculatorIndex ?: 0)?.title ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (activeCalculatorIndex) {
                    0 -> {
                        val history by viewModel.getHistoryForSection("simple_interest").collectAsStateWithLifecycle(emptyList())
                        SimpleInterestScreen(
                            historyList = history,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("simple_interest") }
                        )
                    }
                    1 -> {
                        val history by viewModel.getHistoryForSection("compound").collectAsStateWithLifecycle(emptyList())
                        CompoundInterestScreen(
                            historyList = history,
                            defaultInflation = defaultInflation,
                            defaultTax = defaultTax,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("compound") }
                        )
                    }
                    2 -> {
                        val history by viewModel.getHistoryForSection("loan").collectAsStateWithLifecycle(emptyList())
                        LoanCalculatorScreen(
                            historyList = history,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("loan") }
                        )
                    }
                    3 -> {
                        val history by viewModel.getHistoryForSection("deposit").collectAsStateWithLifecycle(emptyList())
                        BankDepositScreen(
                            historyList = history,
                            defaultInflation = defaultInflation,
                            defaultTax = defaultTax,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("deposit") }
                        )
                    }
                    4 -> {
                        val history by viewModel.getHistoryForSection("inflation").collectAsStateWithLifecycle(emptyList())
                        InflationScreen(
                            historyList = history,
                            defaultInflation = defaultInflation,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("inflation") }
                        )
                    }
                    5 -> {
                        val history by viewModel.getHistoryForSection("gold_fx").collectAsStateWithLifecycle(emptyList())
                        GoldFxScreen(
                            historyList = history,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("gold_fx") }
                        )
                    }
                    6 -> {
                        val history by viewModel.getHistoryForSection("comparison").collectAsStateWithLifecycle(emptyList())
                        ComparisonScreen(
                            historyList = history,
                            defaultInflation = defaultInflation,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("comparison") }
                        )
                    }
                }
            }
        }
    }
}
