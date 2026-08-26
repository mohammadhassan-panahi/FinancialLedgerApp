package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MutualFundEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutualFundsScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val funds by viewModel.mutualFunds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("صندوق‌های سرمایه‌گذاری", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateSurface)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CredifyIndigo.copy(alpha = 0.1f)),
                    border = BorderStroke(0.5.dp, CredifyIndigo.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = CredifyIndigo)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "در این بخش می‌توانید بازدهی و قیمت (NAV) صندوق‌های معتبر را مقایسه کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(funds) { fund ->
                FundCard(fund)
            }
        }
    }
}

@Composable
fun FundCard(fund: MutualFundEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(fund.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text(fund.manager, color = TextSecondary, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when(fund.riskLevel) {
                        "کم‌ریسک" -> EmeraldProfit.copy(alpha = 0.1f)
                        "متوسط" -> GoldAccent.copy(alpha = 0.1f)
                        else -> RoseLoss.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = fund.riskLevel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(fund.riskLevel) {
                            "کم‌ریسک" -> EmeraldProfit
                            "متوسط" -> GoldAccent
                            else -> RoseLoss
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("قیمت صدور (NAV)", color = TextSecondary, fontSize = 12.sp)
                    Text(formatRial(fund.navToman * 10), fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("بازدهی ماهانه", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        formatPercentSigned(fund.returnPercent),
                        fontWeight = FontWeight.Bold,
                        color = if (fund.returnPercent >= 0) EmeraldProfit else RoseLoss
                    )
                }
            }
        }
    }
}
