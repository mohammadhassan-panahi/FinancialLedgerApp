package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.local.GoalEntity
import com.example.ui.LocalIsRial
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.formatRial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("هدف‌گذاری مالی") },
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
            if (goals.isEmpty()) {
                item {
                    Text(
                        "هنوز هدفی تعریف نکردی. مثلاً «خرید گوشی» یا «پس‌انداز برای ماشین».",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onDelete = { viewModel.deleteGoal(goal) },
                        onUpdateProgress = { amount -> viewModel.updateGoalProgress(goal, amount) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, target, category ->
                viewModel.addGoal(title, target, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    onDelete: () -> Unit,
    onUpdateProgress: (Double) -> Unit
) {
    val isRial = LocalIsRial.current
    var showUpdateDialog by remember { mutableStateOf(false) }
    val progress = if (goal.targetAmountRial > 0) (goal.currentSavedRial / goal.targetAmountRial).toFloat().coerceIn(0f, 1f) else 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.title, fontWeight = FontWeight.Bold)
                Text(goal.category, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("هدف: ${formatRial(goal.targetAmountRial, isRial = isRial)}", style = MaterialTheme.typography.bodySmall)
                Text("پس‌انداز: ${formatRial(goal.currentSavedRial, isRial = isRial)}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (goal.isCompleted) EmeraldProfit else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text("${(progress * 100).toInt()}% پیشرفت", style = MaterialTheme.typography.labelSmall)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseLoss)
                }
                TextButton(onClick = { showUpdateDialog = true }) {
                    Text("بروزرسانی مبلغ")
                }
            }
        }
    }

    if (showUpdateDialog) {
        var newAmount by remember { mutableStateOf(goal.currentSavedRial.toString()) }
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("بروزرسانی پس‌انداز") },
            text = {
                OutlinedTextField(value = newAmount, onValueChange = { newAmount = it }, label = { Text("مبلغ فعلی (ریال)") })
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProgress(newAmount.toDoubleOrNull() ?: goal.currentSavedRial)
                    showUpdateDialog = false
                }) { Text("تأیید") }
            },
            dismissButton = { TextButton(onClick = { showUpdateDialog = false }) { Text("انصراف") } }
        )
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت هدف جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان هدف") })
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("مبلغ مورد نیاز (ریال)") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("دسته‌بندی (اختیاری)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val targetVal = target.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && targetVal > 0) {
                    onConfirm(title, targetVal, category.ifBlank { "سایر" })
                }
            }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
