package com.example.ui.tools

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*

private data class ToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun ToolsScreen(
    onOpenCalculators: () -> Unit,
    onOpenRiskAssessment: () -> Unit,
    onOpenOcrScanner: () -> Unit,
    onOpenInvestmentRoadmap: () -> Unit,
    onOpenFinancialHealth: () -> Unit = {},
    onOpenScenarioSimulator: () -> Unit = {},
    onOpenAssetComparison: () -> Unit = {}
) {
    val items = listOf(
        ToolItem(
            "سلامت مالی (Health Score)",
            "سنجش ریسک و تاب‌آوری سبد دارایی",
            Icons.Default.Verified,
            EmeraldCore,
            onOpenFinancialHealth
        ),
        ToolItem(
            "شبیه‌ساز سناریو",
            "اثر رویدادهای بازار بر ثروت شما",
            Icons.Default.Insights,
            IndigoElectric,
            onOpenScenarioSimulator
        ),
        ToolItem(
            "مقایسه پیشرفته",
            "تحلیل رقابتی بازدهی و نوسان دارایی‌ها",
            Icons.Default.Balance,
            RefinedAmberGold,
            onOpenAssetComparison
        ),
        ToolItem(
            "ماشین‌حساب‌های مالی",
            "حباب طلا، تورم، وام و محاسبات مرکب",
            Icons.Default.Calculate,
            IndigoElectric,
            onOpenCalculators
        ),
        ToolItem(
            "اسکن هوشمند رسید",
            "استخراج خودکار جزئیات از فاکتور خرید",
            Icons.Default.DocumentScanner,
            RefinedAmberGold,
            onOpenOcrScanner
        ),
        ToolItem(
            "نقشه راه سرمایه‌گذاری",
            "برنامه زمانی ۱ ساله برای رشد دارایی‌ها",
            Icons.Default.Map,
            Color(0xFF8B5CF6),
            onOpenInvestmentRoadmap
        )
    )

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                color = ObsidianSlate900.copy(alpha = 0.8f)
            ) {
                Text(
                    "جعبه ابزار دارا",
                    modifier = Modifier.padding(16.dp),
                    style = DaraTypography.titleLarge,
                    color = Slate50,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { tool ->
                DaraToolCard(tool)
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun DaraToolCard(tool: ToolItem) {
    Surface(
        onClick = tool.onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tool.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = tool.icon, contentDescription = tool.title, tint = tool.color)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(tool.title, style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text(
                    tool.subtitle,
                    style = DaraTypography.labelSmall,
                    color = Slate400,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Slate600
            )
        }
    }
}
