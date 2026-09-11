package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PortfolioAssetType
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.theme.*
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetFormScreen(
    assetType: PortfolioAssetType,
    onBack: () -> Unit,
    onSubmit: (String, BigDecimal, BigDecimal, Long) -> Unit
) {
    var assetName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("۱") }
    var unitPrice by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val purchaseDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("تأیید") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("انصراف") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val q = PersianNumberUtils.parseAmount(quantity)
    val p = PersianNumberUtils.parseAmount(unitPrice)

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            AddAssetHeader(onBack = onBack)
        },
        bottomBar = {
            SubmitAssetFooter(
                total = q.multiply(p),
                onClick = { onSubmit(assetName, q, p, purchaseDate) }
            )
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
            // Step Indicator
            ProgressInfoCard(assetType = assetType)

            // Asset Visual Card
            AssetVisualHighlight(assetType = assetType)

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PersianNumberTextField(
                    value = assetName,
                    onValueChange = { assetName = it },
                    label = "نام دارایی یا نماد",
                    isDecimalAllowed = false
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PersianNumberTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = "تعداد / مقدار",
                            isDecimalAllowed = true
                        )
                    }
                    Box(modifier = Modifier.weight(1.5f)) {
                        PersianNumberTextField(
                            value = unitPrice,
                            onValueChange = { unitPrice = it },
                            label = "قیمت واحد (ریال)",
                            isDecimalAllowed = true
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSlate800.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.CalendarToday, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                            Text("تاریخ معامله: ${PersianDateUtils.formatJalaliDate(java.util.Date(purchaseDate))}", style = DaraTypography.bodyMedium, color = Slate50)
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, null, tint = IndigoElectric)
                        }
                    }
                }
            }
            
            // Keypad Placeholder / Illustration
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Dialpad, null, tint = Slate600)
                    Text("استفاده از صفحه‌کلید لمسی دارا جهت دقت بیشتر", style = DaraTypography.labelSmall, color = Slate600)
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun AddAssetHeader(onBack: () -> Unit) {
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
                Text("جزئیات معامله", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun ProgressInfoCard(assetType: PortfolioAssetType) {
    Surface(color = ObsidianSlate800, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(36.dp).background(IndigoElectric.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MonetizationOn, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                }
                Column {
                    val typeLabel = when(assetType) {
                        PortfolioAssetType.GOLD -> "طلا و سکه"
                        PortfolioAssetType.CRYPTO -> "رمزارز"
                        else -> "دارایی"
                    }
                    Text("ثبت خرید $typeLabel", style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("گام ۲ از ۲", style = DaraTypography.labelSmall, color = Slate600)
                }
            }
            CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(24.dp), color = EmeraldCore, strokeWidth = 3.dp)
        }
    }
}

@Composable
fun AssetVisualHighlight(assetType: PortfolioAssetType) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate800,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).background(ObsidianSlate700, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Token, null, tint = RefinedAmberGold, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("سکه امامی (طرح جدید)", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                Text("نرخ لحظه‌ای تایید شده", style = DaraTypography.labelSmall, color = EmeraldCore)
            }
        }
    }
}

@Composable
fun SubmitAssetFooter(total: BigDecimal, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = ObsidianSlate900,
        tonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("مبلغ کل خرید:", style = DaraTypography.bodySmall, color = Slate400)
                Text(com.example.util.formatRial(total, isRial = false), style = DaraTypography.titleLarge, color = EmeraldCore, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldCore)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TaskAlt, null, tint = Color.Black)
                    Text("ثبت در پورتفوی هوشمند", style = DaraTypography.titleMedium, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
