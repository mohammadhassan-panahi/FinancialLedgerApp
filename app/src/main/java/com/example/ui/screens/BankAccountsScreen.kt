package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BankAccountEntity
import com.example.ui.LocalIsRial
import com.example.ui.components.NotebookCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatRial

@Composable
fun BankAccountsScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit = {}
) {
    val accounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF8B5CF6)) {
                Icon(Icons.Default.Add, contentDescription = "افزودن حساب", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "مدیریت حساب‌های بانکی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (accounts.isEmpty()) {
                item {
                    NotebookCard {
                        Text(
                            "هنوز حسابی ثبت نکردی. با زدن دکمه + موجودی کارت‌های خود را اضافه کن.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(accounts) { account ->
                    AccountCard(account = account, onDelete = { viewModel.deleteBankAccount(account) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, bank, balance, color ->
                viewModel.addBankAccount(name, bank, balance, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AccountCard(account: BankAccountEntity, onDelete: () -> Unit) {
    val isRial = LocalIsRial.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(android.graphics.Color.parseColor(account.colorHex)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(account.name, fontWeight = FontWeight.Bold)
                    Text(account.bankName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRial(account.currentBalance * 10, isRial = isRial), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    val colors = listOf("#5B85AA", "#4C7A5C", "#C9A24B", "#8B5CF6", "#DC2626")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن حساب بانکی جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام حساب (مثلاً کارت اصلی)") })
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("نام بانک") })
                PersianNumberTextField(value = balance, onValueChange = { balance = it }, label = "موجودی فعلی (تومان)", suffix = "تومان")
                
                Text("انتخاب رنگ", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(color)), CircleShape)
                                .clickable { selectedColor = color }
                                .padding(4.dp)
                        ) {
                            if (selectedColor == color) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.3f), CircleShape))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, bank, balance.toDoubleOrNull() ?: 0.0, selectedColor) }) {
                Text("تأیید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
