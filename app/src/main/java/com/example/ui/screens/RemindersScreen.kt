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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ReminderEntity
import com.example.data.local.ReminderType
import com.example.ui.LocalIsRial
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberUtils
import com.example.util.formatRial
import com.example.worker.ReminderScheduler
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.reminders.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                        onDelete = {
                            viewModel.deleteReminder(item)
                            com.example.worker.ReminderScheduler.cancel(context, item.id)
                        },
                        onMarkPaid = {
                            viewModel.markReminderAsPaid(item)
                            // Already paid — no need to notify about it anymore.
                            com.example.worker.ReminderScheduler.cancel(context, item.id)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, type, dueDate, note ->
                // Client-generated id so the same value can be used both as the Room primary key
                // (SQLite accepts an explicit non-zero rowid instead of auto-generating one) and
                // as the WorkManager unique-work name, keeping the two in sync without needing to
                // await the DB insert to read back an auto-generated id.
                val id = System.currentTimeMillis()
                viewModel.addReminder(title, amount, type, dueDate, note, id = id)
                com.example.worker.ReminderScheduler.schedule(
                    context = context,
                    reminderId = id,
                    title = "سررسید: $title",
                    message = "مبلغ ${formatRial(amount, isRial = true)} امروز سررسید می‌شود.",
                    dueDateMillis = dueDate
                )
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
    val isRial = LocalIsRial.current
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
                formatRial(item.amountRial, isRial = isRial),
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

private val reminderTypeLabels = mapOf(
    ReminderType.INSTALLMENT to "قسط",
    ReminderType.BILL to "قبض",
    ReminderType.CHEQUE to "چک",
    ReminderType.RENT to "اجاره",
    ReminderType.OTHER to "سایر"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, BigDecimal, ReminderType, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ReminderType.INSTALLMENT) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val dueDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("انصراف") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت یادآور جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("مبلغ (ریال)") })

                Text("نوع", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    reminderTypeLabels.forEach { (value, label) ->
                        val selected = type == value
                        TextButton(onClick = { type = value }) {
                            Text(
                                label,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = PersianDateUtils.formatJalaliDate(java.util.Date(dueDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("تاریخ سررسید") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "انتخاب تاریخ")
                        }
                    }
                )

                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("یادداشت") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountVal = PersianNumberUtils.parseAmount(amount)
                if (title.isNotBlank() && amountVal.compareTo(BigDecimal.ZERO) > 0) {
                    onConfirm(title, amountVal, type, dueDate, note)
                }
            }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
