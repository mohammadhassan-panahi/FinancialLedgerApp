package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*

@Composable
fun FinancialHealthScreen(
    onBack: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSimulator: () -> Unit = {}
) {
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            HealthHeader(onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Speedometer Score Card
            ScoreHeroCard(score = 785)

            // AI Advisor Banner
            AiAdvisorBanner(onClick = onNavigateToAi)

            // Analytical Pillars
            Text("فاکتورهای تحلیلی سلامت مالی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HealthFactorCard(
                    title = "تنوع‌بخشی دارایی‌ها",
                    weight = "۳۰٪",
                    percentage = 0.85f,
                    status = "عالی",
                    icon = Icons.Default.PieChart,
                    color = EmeraldCore,
                    desc = "توزیع متوازن میان طلا، نقدینگی و رمزارز تاب‌آوری بالایی ایجاد کرده است."
                )
                HealthFactorCard(
                    title = "نقدینگی در دسترس",
                    weight = "۲۵٪",
                    percentage = 0.42f,
                    status = "پایین",
                    icon = Icons.Default.AccountBalance,
                    color = RefinedAmberGold,
                    desc = "نسبت وجه نقد آزاد به کل ثروت کمتر از حد استاندارد ۵۰٪ است."
                )
                HealthFactorCard(
                    title = "همگرایی با سیگنال‌ها",
                    weight = "۲۵٪",
                    percentage = 0.91f,
                    status = "بسیار بالا",
                    icon = Icons.Default.AutoGraph,
                    color = IndigoElectric,
                    desc = "سبد دارایی با پیش‌بینی روندهای تورمی هماهنگی استثنایی دارد."
                )
            }

            // Improvement Scenarios
            Text("راهکارهای بهبود امتیاز", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            
            ImprovementScenarioItem(
                title = "تزریق ۱۵٪ نقدینگی",
                gain = "+۴۰ امتیاز",
                icon = Icons.Default.AddCircle,
                color = EmeraldCore,
                onClick = onNavigateToSimulator
            )
            ImprovementScenarioItem(
                title = "تعادل مجدد با طلا",
                gain = "+۲۵ امتیاز",
                icon = Icons.Default.SwapHoriz,
                color = IndigoElectric,
                onClick = onNavigateToSimulator
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun HealthHeader(onBack: () -> Unit) {
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
                    Text("Financial Health", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("تحلیل جامع دارا", style = DaraTypography.labelSmall, color = EmeraldCore)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Share, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun ScoreHeroCard(score: Int) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(240.dp, 140.dp), contentAlignment = Alignment.BottomCenter) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawArc(
                        color = ObsidianSlate600,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.horizontalGradient(listOf(RoseCoral, RefinedAmberGold, IndigoElectric, EmeraldCore)),
                        startAngle = 180f,
                        sweepAngle = (score / 1000f) * 180f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(score.toString(), style = DaraTypography.displayLarge, color = Slate50, fontWeight = FontWeight.Black)
                        Text("/ ۱۰۰۰", style = DaraTypography.titleLarge, color = Slate600)
                    }
                }
            }

            Surface(color = EmeraldCore.copy(alpha = 0.1f), shape = RoundedCornerShape(percent = 100)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Verified, null, tint = EmeraldCore, modifier = Modifier.size(16.dp))
                    Text("سطح عالی • کم‌ریسک", style = DaraTypography.labelMedium, color = EmeraldCore, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AiAdvisorBanner(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800,
        border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(44.dp).background(IndigoElectric.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("راهکار هوش مصنوعی دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text("تحلیل سناریوی رسیدن به امتیاز ۸۵۰+", style = DaraTypography.labelSmall, color = Slate400)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = Slate600)
        }
    }
}

@Composable
fun HealthFactorCard(
    title: String,
    weight: String,
    percentage: Float,
    status: String,
    icon: ImageVector,
    color: Color,
    desc: String
) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                        Text("وزن: $weight", style = DaraTypography.labelSmall, color = Slate600)
                    }
                }
                Text(status, style = DaraTypography.labelMedium, color = color, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = ObsidianSlate600
            )
            Text(desc, style = DaraTypography.bodySmall, color = Slate400, lineHeight = 18.sp)
        }
    }
}

@Composable
fun ImprovementScenarioItem(
    title: String,
    gain: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("شبیه‌سازی رشد", style = DaraTypography.labelSmall, color = Slate600)
                }
            }
            Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text(gain, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = DaraTypography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}
