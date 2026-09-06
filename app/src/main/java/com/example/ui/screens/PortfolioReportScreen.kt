package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*

@Composable
fun PortfolioReportScreen(
    onBack: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf("PDF") }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            ReportHeader(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Context Banner
            item {
                ReportContextBanner()
            }

            // Date Range
            item {
                ReportDateRangeSection()
            }

            // Export Formats
            item {
                Text("فرمت خروجی پرونده", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormatCard(
                        title = "PDF رسمی",
                        size = "۲.۴ مگ",
                        icon = Icons.Default.PictureAsPdf,
                        isSelected = selectedFormat == "PDF",
                        onClick = { selectedFormat = "PDF" },
                        modifier = Modifier.weight(1f)
                    )
                    FormatCard(
                        title = "اکسل",
                        size = "۴۵۰ کیلوبایت",
                        icon = Icons.Default.TableChart,
                        isSelected = selectedFormat == "XLSX",
                        onClick = { selectedFormat = "XLSX" },
                        modifier = Modifier.weight(1f)
                    )
                    FormatCard(
                        title = "تصویر",
                        size = "۱.۱ مگ",
                        icon = Icons.Default.Image,
                        isSelected = selectedFormat == "PNG",
                        onClick = { selectedFormat = "PNG" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Document Preview
            item {
                Text("پیش‌نمایش سند رسمی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                DocumentPreviewCard()
            }

            // Action Button
            item {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RefinedAmberGold)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Download, null, tint = ObsidianSlate900)
                        Text("دریافت کارنامه رسمی ($selectedFormat)", style = DaraTypography.titleMedium, color = ObsidianSlate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ReportHeader(onBack: () -> Unit) {
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
                Text("گزارش‌گیری و خروجی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.VerifiedUser, null, tint = RefinedAmberGold)
            }
        }
    }
}

@Composable
fun ReportContextBanner() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(48.dp).background(RefinedAmberGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Analytics, null, tint = RefinedAmberGold)
            }
            Column {
                Text("تنظیم بازه و خروجی گزارش", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                Text("کارنامه جامع بازدهی دارایی‌ها و ترازنامه", style = DaraTypography.labelSmall, color = Slate400)
            }
        }
    }
}

@Composable
fun ReportDateRangeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("بازه زمانی گزارش", style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
            Text("تقویم خورشیدی", style = DaraTypography.labelSmall, color = EmeraldCore)
        }
        
        Surface(color = ObsidianSlate800, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateRangeBox(label = "از تاریخ:", date = "۱ مرداد ۱۴۰۳", modifier = Modifier.weight(1f))
                DateRangeBox(label = "تا تاریخ:", date = "۳۱ مرداد ۱۴۰۳", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DateRangeBox(label: String, date: String, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = DaraTypography.labelSmall, color = Slate600)
        Text(date, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FormatCard(title: String, size: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) IndigoElectric.copy(alpha = 0.2f) else ObsidianSlate800,
        border = BorderStroke(1.dp, if (isSelected) IndigoElectric else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = if (isSelected) IndigoElectric else Slate600)
            Column {
                Text(title, style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
                Text(size, style = DaraTypography.labelSmall, color = Slate600)
            }
        }
    }
}

@Composable
fun DocumentPreviewCard() {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(24.dp).background(RefinedAmberGold, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                        Text("د", color = ObsidianSlate900, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Text("کارنامه جامع پورتفوی دارا", style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
                }
                Text("۳۱ مرداد ۱۴۰۳", style = DaraTypography.labelSmall, color = Slate600)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f).background(ObsidianSlate700, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text("سود خالص ماهانه:", style = DaraTypography.labelSmall, color = Slate400)
                    Text("+۳۸,۴۵۰,۰۰۰", style = DaraTypography.titleMedium, color = EmeraldCore, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f).background(ObsidianSlate700, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text("پرسودترین دارایی:", style = DaraTypography.labelSmall, color = Slate400)
                    Text("طلای ۱۸ عیار", style = DaraTypography.titleMedium, color = RefinedAmberGold, fontWeight = FontWeight.Bold)
                }
            }
            
            Text("تأییدیه رمزنگاری دارا v4.2", style = DaraTypography.labelSmall, color = Slate600)
        }
    }
}
