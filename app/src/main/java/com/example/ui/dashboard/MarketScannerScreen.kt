package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                title = { Text("دیده‌بان هوشمند بازار (Scanner)", fontWeight = FontWeight.Bold) },
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
                        Text(
                            text = "تحلیل تکنیکال ۱۰ ارز برتر بازار بر اساس استراتژی میانگین متحرک و RSI",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Right
                        )
                    }

                    items(opportunities) { opportunity ->
                        OpportunityCard(
                            opportunity = opportunity,
                            onWhyClick = { viewModel.fetchAiReport(opportunity) }
                        )
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
                isLoading = aiLoading
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
                ScoreIndicator(score = analysis.score)
                
                SignalBadge(signal = analysis.signal)
                
                Button(
                    onClick = onWhyClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("چرا؟", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ScoreIndicator(score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("امتیاز", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp,
                color = when {
                    score >= 70 -> Color(0xFF4CAF50)
                    score >= 40 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SignalBadge(signal: AnalysisSignal) {
    val (text, color) = when (signal) {
        AnalysisSignal.STRONG_BUY -> "خرید قوی" to Color(0xFF2E7D32)
        AnalysisSignal.BUY_PULLBACK -> "خرید در اصلاح" to Color(0xFF4CAF50)
        AnalysisSignal.BREAKOUT_WATCH -> "واچ‌لیست شکست" to Color(0xFF8BC34A)
        AnalysisSignal.HOLD -> "نگهداری" to Color(0xFFFFA000)
        AnalysisSignal.WAIT -> "صبر" to Color(0xFF9E9E9E)
        AnalysisSignal.SELL_PARTIAL -> "فروش پله‌ای" to Color(0xFFE64A19)
        AnalysisSignal.SELL_NOW -> "فروش فوری" to Color(0xFFD32F2F)
        AnalysisSignal.AVOID -> "دوری از معامله" to Color(0xFFB71C1C)
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
    isLoading: Boolean
) {
    if (opportunity == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "تحلیل هوشمند Gemini برای ${opportunity.asset.symbol}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("در حال پردازش داده‌های تکنیکال توسط هوش مصنوعی...", textAlign = TextAlign.Center)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = opportunity.aiReport ?: "خطا در دریافت گزارش",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Justify
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "سلب مسئولیت: این تحلیل صرفاً بر اساس داده‌های تکنیکال و هوش مصنوعی بوده و به منزله پیشنهاد قطعی خرید یا فروش نیست.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
