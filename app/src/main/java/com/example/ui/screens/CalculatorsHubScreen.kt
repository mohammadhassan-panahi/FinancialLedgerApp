package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
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
    defaultTax: Double = 0.0,
    holdings: List<com.example.data.repository.HoldingSummary> = emptyList(),
    marketRates: List<com.example.data.local.MarketRateEntity> = emptyList(),
    cryptoAssets: List<com.example.data.local.CryptoAssetEntity> = emptyList()
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
        ),
        CalculatorItemInfo(
            title = "۸. حباب سکه و طلا",
            description = "مقایسه قیمت بازار با ارزش ذاتی بر مبنای انس جهانی و دلار — کی خرید منطقیه؟",
            category = "بازار",
            icon = Icons.Default.QueryStats,
            iconBgColor = Color(0xFFB45309)
        ),
        CalculatorItemInfo(
            title = "۹. اجرت و مالیات طلا",
            description = "قیمت تمام‌شده طلای نو و دست‌دوم بر اساس فرمول اتحادیه طلا",
            category = "بازار",
            icon = Icons.Default.Paid,
            iconBgColor = Color(0xFF15803D)
        ),
        CalculatorItemInfo(
            title = "۱۰. اگر آن روز می‌خریدی",
            description = "محاسبه سود فرضی: اگر فلان تاریخ سرمایه‌ات را خرج فلان دارایی می‌کردی",
            category = "تحلیلی",
            icon = Icons.Default.History,
            iconBgColor = Color(0xFF7E22CE)
        ),
        CalculatorItemInfo(
            title = "۱۱. شبیه‌ساز سناریو",
            description = "اگر دلار یا طلا رشد/ریزد، ارزش سبد تو چقدر می‌شود؟ (متصل به سبد واقعی)",
            category = "تحلیلی",
            icon = Icons.Default.DeviceThermostat,
            iconBgColor = Color(0xFFBE123C)
        )
    )

    if (activeCalculatorIndex == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSlateSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back navigation to Home
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "بازگشت",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "بازگشت",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.clickable { onBack() }
                )
            }

            // Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
                border = BorderStroke(0.5.dp, SlateBorderLight)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CredifyIndigo.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = CredifyIndigo,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "ابزارهای هوشمند مالی",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "محاسبات دقیق بازار در دستان شما",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(calculatorsList) { index, calc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { activeCalculatorIndex = index },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
                        border = BorderStroke(0.5.dp, SlateBorderLight)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = calc.iconBgColor.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(calc.icon, null, tint = calc.iconBgColor, modifier = Modifier.size(24.dp))
                                }
                            }
                            
                            Column {
                                Text(
                                    text = calc.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = calc.category,
                                    fontSize = 10.sp,
                                    color = calc.iconBgColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Active Sub-Calculator View
        Column(modifier = Modifier.fillMaxSize().background(DarkSlateSurface)) {
            // Top Navigation Back Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSlateSecondary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeCalculatorIndex = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بازگشت به فهرست",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.clickable { activeCalculatorIndex = null }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = calculatorsList.getOrNull(activeCalculatorIndex ?: 0)?.title ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
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
                            marketRates = marketRates,
                            cryptoAssets = cryptoAssets,
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
                    7 -> {
                        val history by viewModel.getHistoryForSection("gold_bubble").collectAsStateWithLifecycle(emptyList())
                        GoldBubbleScreen(
                            historyList = history,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("gold_bubble") }
                        )
                    }
                    8 -> {
                        val history by viewModel.getHistoryForSection("gold_wage").collectAsStateWithLifecycle(emptyList())
                        GoldWageScreen(
                            historyList = history,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("gold_wage") }
                        )
                    }
                    9 -> {
                        val history by viewModel.getHistoryForSection("retrospective").collectAsStateWithLifecycle(emptyList())
                        RetrospectiveScreen(
                            historyList = history,
                            currencyUnit = currencyUnit,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("retrospective") }
                        )
                    }
                    10 -> {
                        val history by viewModel.getHistoryForSection("scenario").collectAsStateWithLifecycle(emptyList())
                        ScenarioScreen(
                            holdings = holdings,
                            historyList = history,
                            onAddHistory = { viewModel.addHistory(it) },
                            onDeleteHistory = { viewModel.deleteHistory(it) },
                            onClearHistory = { viewModel.clearSectionHistory("scenario") }
                        )
                    }
                }
            }
        }
    }
}
