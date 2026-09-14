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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MutualFundEntity
import com.example.data.local.MutualFundType
import com.example.data.local.PortfolioAssetType
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import java.math.BigDecimal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutualFundDetailScreen(
    viewModel: PortfolioViewModel,
    fund: MutualFundEntity,
    onBack: () -> Unit,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit
) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val isRial = LocalIsRial.current
    
    // Find user's holding in this fund
    val userHolding = remember(holdings, fund) {
        holdings.find { it.assetCode == fund.id || it.assetName == fund.name }
    }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text(fund.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSlate900)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Hero Card: Current NAV
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("خالص ارزش دارایی‌ها (NAV)", style = DaraTypography.labelSmall, color = Slate400)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        formatRial(fund.navToman.multiply(BigDecimal("10")), isRial = isRial),
                        style = DaraTypography.displaySmall,
                        color = Slate50,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = (if (fund.returnPercent >= BigDecimal.ZERO) EmeraldCore else RoseCoral).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(percent = 100)
                    ) {
                        Text(
                            text = "بازدهی ماهانه: ${formatPercentSigned(fund.returnPercent)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = DaraTypography.labelMedium,
                            color = if (fund.returnPercent >= BigDecimal.ZERO) EmeraldCore else RoseCoral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. User Holdings in this fund
            userHolding?.let { h ->
                Text("دارایی‌های من", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("تعداد واحدها", style = DaraTypography.labelSmall, color = Slate400)
                            Text(com.example.util.PersianNumberUtils.formatDecimal(h.quantity), style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ارزش فعلی", style = DaraTypography.labelSmall, color = Slate400)
                            Text(formatRial(h.currentValueRial, isRial = isRial), style = DaraTypography.titleLarge, color = EmeraldCore, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Fund Info Table
            Text("اطلاعات شناسنامه‌ای", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow("نوع صندوق", if (fund.fundType == MutualFundType.ETF) "قابل معامله (ETF)" else "صدور و ابطالی")
                    InfoRow("مدیر صندوق", fund.manager)
                    InfoRow("تاریخ تأسیس", fund.inceptionDate.ifBlank { "نامشخص" })
                    InfoRow("کل دارایی‌ها", if (fund.totalAssetsToman > BigDecimal.ZERO) formatRial(fund.totalAssetsToman.multiply(BigDecimal("10")), isRial = isRial) else "در حال دریافت...")
                    InfoRow("سطح ریسک", fund.riskLevel)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 4. Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBuyClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric)
                ) {
                    Text(if (fund.fundType == MutualFundType.ETF) "خرید از بورس" else "صدور واحد", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onSellClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RoseCoral)
                ) {
                    Text(if (fund.fundType == MutualFundType.ETF) "فروش در بورس" else "ابطال واحد", color = RoseCoral, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = DaraTypography.bodyMedium, color = Slate400)
        Text(value, style = DaraTypography.bodyMedium, color = Slate50, fontWeight = FontWeight.SemiBold)
    }
}
