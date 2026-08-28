package com.example.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class ToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

/**
 * Hub screen for the "ابزارها" (Tools) bottom-nav tab.
 * Replaces the old empty placeholder ("Tools Screen") with real entry points
 * to features that already existed in the codebase but had no way to be reached.
 */
@Composable
fun ToolsScreen(
    onOpenCalculators: () -> Unit,
    onOpenRiskAssessment: () -> Unit,
    onOpenOcrScanner: () -> Unit,
    onOpenInvestmentRoadmap: () -> Unit
) {
    val items = listOf(
        ToolItem(
            "ماشین‌حساب‌های مالی",
            "وام، سود مرکب، تورم، حباب طلا، اجرت، سناریو و بیشتر",
            Icons.Default.Calculate,
            Color(0xFF6366F1),
            onOpenCalculators
        ),
        ToolItem(
            "ارزیابی ریسک‌پذیری",
            "شناخت پروفایل سرمایه‌گذاری شما",
            Icons.Default.Psychology,
            Color(0xFF10B981),
            onOpenRiskAssessment
        ),
        ToolItem(
            "اسکن هوشمند رسید",
            "استخراج خودکار جزئیات از فاکتور خرید",
            Icons.Default.DocumentScanner,
            Color(0xFFF59E0B),
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

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "ابزارها",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(items) { tool ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { tool.onClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(tool.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = tool.icon, contentDescription = tool.title, tint = tool.color)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(tool.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            tool.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
