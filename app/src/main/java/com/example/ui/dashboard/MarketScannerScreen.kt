package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.analysis.AnalysisSignal
import com.example.ui.components.CryptoIcon
import kotlinx.coroutines.launch

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScannerScreen(
    viewModel: MarketScannerViewModel
) {
    val opportunities by viewModel.opportunities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedOp by viewModel.selectedOpportunity.collectAsState()
    val aiLoading by viewModel.aiReportLoading.collectAsState()
    
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOp) {
        if (selectedOp != null) {
            showSheet = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("رتبه‌بندی فرصت‌های بازار", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("تحلیل هوشمند ۱۰ ارز برتر", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanMarket() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && opportunities.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "ارزها بر اساس امتیاز ترکیبی (روند، حجم، RSI و ریسک) رتبه‌بندی شده‌اند. رتبه ۱ بهترین فرصت فعلی است.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Right
                            )
                        }
                    }

                    itemsIndexed(opportunities) { index, opportunity ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                OpportunityCard(
                                    opportunity = opportunity,
                                    onWhyClick = { viewModel.selectOpportunity(opportunity) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false
                viewModel.clearSelection()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            AiReportContent(
                opportunity = selectedOp,
                isLoading = aiLoading,
                onDeepAnalysisRequested = { selectedOp?.let { viewModel.fetchAiDeepAnalysis(it) } }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun OpportunityCard(
    opportunity: CryptoOpportunity,
    onWhyClick: () -> Unit
) {
    val asset = opportunity.asset
    val analysis = opportunity.analysis
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryptoIcon(cmcId = asset.cmcId, symbol = asset.symbol)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = asset.symbol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", asset.priceUsd ?: 0.0)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val change = asset.percentChange24h ?: 0.0
                    Text(
                        text = "${if (change >= 0) "+" else ""}${String.format(Locale.US, "%.2f", change)}%",
                        color = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreIndicator(
                    label = "فرصت",
                    score = analysis.opportunityScore
                )
                
                SignalBadge(signal = analysis.signal)

                ScoreIndicator(
                    label = "ریسک",
                    score = analysis.riskScore,
                    isRisk = true
                )
                
                Button(
                    onClick = onWhyClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("جزئیات", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ScoreIndicator(label: String, score: Int, isRisk: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp,
                color = if (isRisk) {
                    when {
                        score >= 70 -> Color(0xFFF44336) // High Risk
                        score >= 40 -> Color(0xFFFFC107)
                        else -> Color(0xFF4CAF50)
                    }
                } else {
                    when {
                        score >= 70 -> Color(0xFF4CAF50) // High Opportunity
                        score >= 40 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SignalBadge(signal: AnalysisSignal) {
    val (text, color) = when (signal) {
        AnalysisSignal.STRONG_BUY -> "خرید قوی" to Color(0xFF2E7D32)
        AnalysisSignal.BUY_ON_PULLBACK -> "خرید در اصلاح" to Color(0xFF4CAF50)
        AnalysisSignal.BREAKOUT_WATCH -> "واچ‌لیست شکست" to Color(0xFF8BC34A)
        AnalysisSignal.HOLD -> "نگهداری" to Color(0xFFFFA000)
        AnalysisSignal.WAIT -> "صبر" to Color(0xFF9E9E9E)
        AnalysisSignal.SELL_PARTIAL -> "فروش پله‌ای" to Color(0xFFE64A19)
        AnalysisSignal.SELL -> "فروش" to Color(0xFFD32F2F)
        AnalysisSignal.AVOID -> "دوری از معامله" to Color(0xFFB71C1C)
        AnalysisSignal.INSUFFICIENT_DATA -> "داده ناکافی" to Color(0xFF607D8B)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AiReportContent(
    opportunity: CryptoOpportunity?,
    isLoading: Boolean,
    onDeepAnalysisRequested: () -> Unit
) {
    if (opportunity == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "تحلیل هوشمند برای ${opportunity.asset.symbol}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Always show the Local (Algorithmic) Report
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "گزارش الگوریتم (آفلاین)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = opportunity.localReport,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (opportunity.aiReport != null) {
            Text(
                text = "تحلیل عمیق هوش مصنوعی (Gemini):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = opportunity.aiReport,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp
                )
            }
        } else if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("در حال دریافت تحلیل عمیق از هوش مصنوعی...", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Button(
                onClick = onDeepAnalysisRequested,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("دریافت تحلیل عمیق‌تر (نیاز به اینترنت)")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "سلب مسئولیت: این تحلیل صرفاً بر اساس داده‌های تکنیکال و الگوریتم‌های برنامه بوده و به منزله پیشنهاد قطعی خرید یا فروش نیست.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
