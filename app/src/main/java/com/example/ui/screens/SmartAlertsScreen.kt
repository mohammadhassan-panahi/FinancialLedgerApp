package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*

@Composable
fun SmartAlertsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            AlertsHeader(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Metrics Overview
            item {
                AlertsMetricsBanner()
            }

            // Section 2: Active Alerts List
            item {
                Text("فهرست دیده‌بان‌های فعال", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            items(2) {
                ActiveAlertCard(
                    title = if (it == 0) "طلای ۱۸ عیار" else "بیت‌کوین (BTC)",
                    desc = if (it == 0) "عبور از مرز ۴,۵۰۰,۰۰۰ تومان" else "نوسان ناگهانی ±۵٪ در یک ساعت",
                    icon = if (it == 0) Icons.Default.MonetizationOn else Icons.Default.CurrencyBitcoin,
                    color = if (it == 0) RefinedAmberGold else IndigoElectric,
                    isVip = it == 0
                )
            }

            // Section 3: Notification Preview
            item {
                Text("پیش‌نمایش اعلان سیستم", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                NotificationPreviewHud()
            }

            // Section 4: Create Alert Studio
            item {
                CreateAlertStudio()
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun AlertsHeader(onBack: () -> Unit) {
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
                    Text("هشدارهای هوشمند", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("Smart Alerts Hub", style = DaraTypography.labelSmall, color = EmeraldCore)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Tune, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun AlertsMetricsBanner() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).background(IndigoElectric.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Radar, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("پایشگر دارایی‌های لوکس", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("پردازش بی‌درنگ سبد ثروت", style = DaraTypography.labelSmall, color = Slate400)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem(label = "هشدارهای فعال", value = "۴", subValue = "حلقه زنده")
                MetricItem(label = "رویدادهای امروز", value = "۲", subValue = "تحقق‌یافته")
                MetricItem(label = "سهمیه پیامک", value = "۹۸٪", subValue = "VIP")
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, subValue: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = DaraTypography.labelSmall, color = Slate600)
        Text(value, style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Black)
        Text(subValue, style = DaraTypography.labelSmall, color = EmeraldCore)
    }
}

@Composable
fun ActiveAlertCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    isVip: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSlate800.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                    if (isVip) {
                        Surface(color = RoseCoral.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("VIP", modifier = Modifier.padding(horizontal = 4.dp), style = DaraTypography.labelSmall, color = RoseCoral, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(desc, style = DaraTypography.bodySmall, color = Slate50)
                Text("نرخ کنونی همگام با بازار", style = DaraTypography.labelSmall, color = Slate600)
            }
            Switch(
                checked = true,
                onCheckedChange = { },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = color,
                    checkedTrackColor = color.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun NotificationPreviewHud() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSlate800.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(24.dp).background(IndigoElectric, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Diamond, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text("دارا • مدیریت ثروت", style = DaraTypography.labelSmall, color = Slate50, fontWeight = FontWeight.Bold)
                }
                Text("اکنون", style = DaraTypography.labelSmall, color = Slate600)
            }
            Text("هشدار نوسان فوری: طلای ۱۸ عیار", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
            Text("نرخ با +۳.۲٪ افزایش از مرز مشخص شده عبور کرد.", style = DaraTypography.bodySmall, color = Slate400)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = IndigoElectric, shape = RoundedCornerShape(8.dp)) {
                    Text("معامله سریع", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = DaraTypography.labelSmall, color = Color.White)
                }
                Surface(color = ObsidianSlate600, shape = RoundedCornerShape(8.dp)) {
                    Text("مشاهده نمودار", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = DaraTypography.labelSmall, color = Slate400)
                }
            }
        }
    }
}

@Composable
fun CreateAlertStudio() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.AddAlert, null, tint = EmeraldCore)
                    Text("تنظیم هشدار جدید", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric)
            }
            
            // Asset Selector Placeholder
            Surface(color = ObsidianSlate700, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("انتخاب دارایی هدف", style = DaraTypography.bodyMedium, color = Slate400)
                    Icon(Icons.Default.ExpandMore, null, tint = Slate600)
                }
            }
            
            // Price Target Slider Mock
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نرخ هدف مدنظر (تومان)", style = DaraTypography.labelSmall, color = Slate600)
                Text("۴,۵۰۰,۰۰۰", style = DaraTypography.headlineMedium, color = IndigoElectric, fontWeight = FontWeight.Black)
                Slider(value = 0.6f, onValueChange = {}, colors = SliderDefaults.colors(thumbColor = IndigoElectric, activeTrackColor = IndigoElectric))
            }
            
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric)
            ) {
                Text("فعال‌سازی دیده‌بان هوشمند", style = DaraTypography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
