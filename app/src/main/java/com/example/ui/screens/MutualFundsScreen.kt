package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MutualFundEntity
import com.example.data.local.MutualFundType
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatPercentSigned
import com.example.util.formatRial
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutualFundsScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onFundClick: (MutualFundEntity) -> Unit
) {
    val funds by viewModel.mutualFunds.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("همه") }

    val filteredFunds = remember(funds, selectedFilter) {
        when (selectedFilter) {
            "ETF" -> funds.filter { it.fundType == MutualFundType.ETF }
            "صدور و ابطالی" -> funds.filter { it.fundType == MutualFundType.ISSUANCE_REDEMPTION }
            else -> funds
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("صندوق‌های سرمایه‌گذاری", color = Slate50, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Slate50)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSlate900)
            )
        },
        containerColor = ObsidianSlate900
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("همه", "ETF", "صدور و ابطالی").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoElectric,
                            selectedLabelColor = Color.White,
                            labelColor = Slate400
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = IndigoElectric)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "ETFها در بورس معامله می‌شوند، اما صدور و ابطالی‌ها نیاز به واریز مستقیم به حساب صندوق دارند.",
                                style = DaraTypography.bodySmall,
                                color = Slate400
                            )
                        }
                    }
                }

                items(filteredFunds) { fund ->
                    FundCard(fund, onClick = { onFundClick(fund) })
                }
            }
        }
    }
}

@Composable
fun FundCard(fund: MutualFundEntity, onClick: () -> Unit) {
    val isRial = LocalIsRial.current
    DaraGlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(fund.name, fontWeight = FontWeight.Bold, color = Slate50, fontSize = 16.sp)
                    Text(fund.manager, color = Slate400, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (fund.fundType == MutualFundType.ETF) IndigoElectric else EmeraldCore).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (fund.fundType == MutualFundType.ETF) "قابل معامله" else "صدور/ابطالی",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = DaraTypography.labelSmall,
                        color = if (fund.fundType == MutualFundType.ETF) IndigoElectric else EmeraldCore,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("قیمت هر واحد (NAV)", color = Slate400, fontSize = 12.sp)
                    Text(formatRial(fund.navToman.multiply(BigDecimal.valueOf(10)), isRial = isRial), fontWeight = FontWeight.Bold, color = Slate50)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("بازدهی ماهانه", color = Slate400, fontSize = 12.sp)
                    Text(
                        formatPercentSigned(fund.returnPercent),
                        fontWeight = FontWeight.Bold,
                        color = if (fund.returnPercent >= BigDecimal.ZERO) EmeraldCore else RoseCoral
                    )
                }
            }
        }
    }
}
