package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DaraAnalysisCard(
    score: Int,
    reason: String,
    liquidity: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate800,
        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                    Text("تحلیل هوشمند دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                }
                
                Surface(
                    color = (if (score >= 50) EmeraldCore else RoseCoral).copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "$score / ۱۰۰",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = DaraTypography.labelMedium,
                        color = if (score >= 50) EmeraldCore else RoseCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = reason,
                style = DaraTypography.bodySmall,
                color = Slate400,
                lineHeight = 18.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AnalysisMiniInfo(label = "نقدشوندگی", value = liquidity, color = EmeraldCore)
                AnalysisMiniInfo(label = "ریسک", value = if (score >= 70) "پایین" else if (score >= 40) "متوسط" else "بالا", color = if (score >= 70) EmeraldCore else if (score >= 40) RefinedAmberGold else RoseCoral)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, null, tint = Slate600, modifier = Modifier.size(14.dp))
                Text(
                    "این تحلیل صرفاً بر اساس داده‌های ریاضی بازار بوده و پیشنهاد مالی قطعی نیست.",
                    style = DaraTypography.labelSmall,
                    color = Slate600,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun AnalysisMiniInfo(label: String, value: String, color: Color) {
    Column {
        Text(label, style = DaraTypography.labelSmall, color = Slate600)
        Text(value, style = DaraTypography.labelMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
