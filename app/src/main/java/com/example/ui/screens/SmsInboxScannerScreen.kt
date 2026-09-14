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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PendingTransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsInboxScannerScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val pendingTxs by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("اسکن پیامک‌های بانکی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    Button(
                        onClick = { viewModel.scanSmsInbox(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اسکن پیامک‌ها")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (pendingTxs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SmsFailed, null, modifier = Modifier.size(64.dp), tint = Slate600)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("تراکنش جدیدی شناسایی نشده است.", color = Slate400)
                        Text("روی دکمه اسکن کلیک کنید.", style = DaraTypography.labelSmall, color = Slate600)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "تراکنش‌های پیشنهادی (${PersianNumberUtils.toPersianDigits(pendingTxs.size.toString())} مورد)",
                            style = DaraTypography.titleMedium,
                            color = Slate50,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(pendingTxs) { tx ->
                        PendingTxCard(
                            tx = tx,
                            onConfirm = { viewModel.confirmPendingTransaction(tx) },
                            onDismiss = { viewModel.dismissPendingTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingTxCard(
    tx: PendingTransactionEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = (if (tx.type == TransactionType.DEPOSIT) EmeraldCore else RoseCoral).copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (tx.type == TransactionType.DEPOSIT) Icons.Default.Add else Icons.Default.Remove,
                                null,
                                tint = if (tx.type == TransactionType.DEPOSIT) EmeraldCore else RoseCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(tx.bankName ?: "بانک نامشخص", style = DaraTypography.labelSmall, color = Slate400)
                        Text(tx.title, style = DaraTypography.bodyLarge, color = Slate50, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    PersianDateUtils.formatRelativeTime(tx.timestamp),
                    style = DaraTypography.labelSmall,
                    color = Slate600
                )
            }

            Text(
                text = PersianNumberUtils.formatCurrency(tx.amount, isRial = false),
                style = DaraTypography.headlineSmall,
                color = if (tx.type == TransactionType.DEPOSIT) EmeraldCore else Slate50,
                fontWeight = FontWeight.Black
            )

            tx.rawSmsContent?.let {
                Surface(
                    color = ObsidianSlate900.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(8.dp),
                        style = DaraTypography.labelSmall,
                        color = Slate600,
                        lineHeight = 16.sp
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldCore),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تأیید و ثبت", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Slate600)
                ) {
                    Text("رد کردن", color = Slate400)
                }
            }
        }
    }
}
