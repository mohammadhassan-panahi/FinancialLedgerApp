package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiAnalysisViewModel
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentRoadmapScreen(
    viewModel: AiAnalysisViewModel,
    onBack: () -> Unit
) {
    val localAnalysis by viewModel.localAnalysis.collectAsStateWithLifecycle()
    
    // Monthly steps for 1 year
    val steps = remember {
        listOf(
            RoadmapStep("ماه ۱-۳", "تمرکز بر نقدینگی", "ایجاد صندوق اضطراری و ورود پله‌ای به طلا", Icons.Default.Shield),
            RoadmapStep("ماه ۴-۶", "تنوع‌بخشی سبد", "ورود به بورس ایران و صندوق‌های سهامی", Icons.Default.Analytics),
            RoadmapStep("ماه ۷-۹", "رشد سرمایه", "افزودن رمزارزهای معتبر (BTC/ETH) به سبد", Icons.Default.TrendingUp),
            RoadmapStep("ماه ۱۰-۱۲", "بهینه‌سازی", "بازبینی سود و ضرر و تبدیل سودها به دارایی امن", Icons.Default.CheckCircle)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نقشه راه ۱ ساله سرمایه‌گذاری") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        containerColor = DarkSlateSurface
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CredifyIndigo.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CredifyIndigo.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "این نقشه راه بر اساس استراتژی رشد میان‌مدت و حفظ ارزش پول در مقابل تورم طراحی شده است.",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            items(steps) { step ->
                RoadmapStepCard(step)
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "توصیه استراتژیک:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary)
                ) {
                    Text(
                        text = "هر ماه حداقل ۲۰٪ از درآمد خود را طبق جدول بالا تقسیم کنید: ۵۰٪ طلا، ۳۰٪ بورس، ۲۰٪ رمزارز. این کار باعث کاهش ریسک تورم و افزایش ثروت در بلندمدت می‌شود.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

data class RoadmapStep(
    val time: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun RoadmapStepCard(step: RoadmapStep) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CredifyIndigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(step.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
                    .background(CredifyIndigo.copy(alpha = 0.3f))
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(step.time, style = MaterialTheme.typography.labelSmall, color = CredifySky)
                Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(step.description, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
            }
        }
    }
}
