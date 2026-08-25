package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DebtCreditEntity
import com.example.data.local.DebtCreditType
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtCreditsScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.debtCredits.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بده و بستان") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                item {
                    Text(
                        "لیست طلب‌ها و بدهی‌ها خالی است.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(items) { item ->
                    DebtCreditCard(
                        item = item,
                        onDelete = { viewModel.deleteDebtCredit(item) },
                        onSettle = { viewModel.settleDebtCredit(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddDebtCreditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, type, desc ->
                viewModel.addDebtCredit(name, amount, type, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DebtCreditCard(
    item: DebtCreditEntity,
    onDelete: () -> Unit,
    onSettle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.personName, fontWeight = FontWeight.Bold)
                Text(
                    if (item.type == DebtCreditType.CREDIT) "طلب" else "بدهی",
                    color = if (item.type == DebtCreditType.CREDIT) EmeraldProfit else RoseLoss
                )
            }
            Text(
                formatRial(item.amountRial),
                style = MaterialTheme.typography.titleMedium,
                color = if (item.isSettled) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            if (item.description.isNotBlank()) {
                Text(item.description, style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseLoss)
                }
                if (!item.isSettled) {
                    TextButton(onClick = onSettle) {
                        Text("تسویه شد")
                    }
                } else {
                    Text("تسویه شده", color = EmeraldProfit, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun AddDebtCreditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, DebtCreditType, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtCreditType.CREDIT) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت مورد جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام شخص") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("مبلغ (ریال)") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == DebtCreditType.CREDIT, onClick = { type = DebtCreditType.CREDIT })
                    Text("طلب")
                    RadioButton(selected = type == DebtCreditType.DEBT, onClick = { type = DebtCreditType.DEBT })
                    Text("بدهی")
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("توضیحات") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && amountVal > 0) {
                    onConfirm(name, amountVal, type, description)
                }
            }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
