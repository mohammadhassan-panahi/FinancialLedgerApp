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
import com.example.data.local.ReminderEntity
import com.example.data.local.ReminderType
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianDateUtils
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.reminders.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("یادآور اقساط و چک") },
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
                        "هنوز یادآوری ثبت نکردی.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(items) { item ->
                    ReminderCard(
                        item = item,
                        onDelete = { viewModel.deleteReminder(item) },
                        onMarkPaid = { viewModel.markReminderAsPaid(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, type, dueDate, note ->
                viewModel.addReminder(title, amount, type, dueDate, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderCard(
    item: ReminderEntity,
    onDelete: () -> Unit,
    onMarkPaid: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.title, fontWeight = FontWeight.Bold)
                Text(
                    when(item.type) {
                        ReminderType.INSTALLMENT -> "قسط"
                        ReminderType.BILL -> "قبض"
                        ReminderType.CHEQUE -> "چک"
                        ReminderType.RENT -> "اجاره"
                        ReminderType.OTHER -> "سایر"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                formatRial(item.amountRial),
                style = MaterialTheme.typography.titleMedium,
                color = if (item.isPaid) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "سررسید: ${PersianDateUtils.formatJalaliDate(java.util.Date(item.dueDate))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseLoss)
                }
                if (!item.isPaid) {
                    TextButton(onClick = onMarkPaid) {
                        Text("پرداخت شد")
                    }
                } else {
                    Text("پرداخت شده", color = EmeraldProfit, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, ReminderType, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    // Simplify for now: use current time as due date. In a real app, use a DatePicker.
    val dueDate = System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت یادآور جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("مبلغ (ریال)") })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("یادداشت") })
                Text("تاریخ سررسید در نسخه فعلی امروز تنظیم می‌شود (به‌زودی انتخاب‌گر تاریخ اضافه می‌شود).", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && amountVal > 0) {
                    onConfirm(title, amountVal, ReminderType.INSTALLMENT, dueDate, note)
                }
            }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
