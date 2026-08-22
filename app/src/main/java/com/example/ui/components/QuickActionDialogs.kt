package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionType
import com.example.ui.theme.CredifyIndigo

@Composable
fun TransactionActionDialog(
    actionType: QuickActionType,
    onDismiss: () -> Unit,
    onConfirmTransaction: (title: String, amount: Double, type: TransactionType, category: String) -> Unit
) {
    var titleInput by remember {
        mutableStateOf(
            when (actionType) {
                QuickActionType.DEPOSIT -> "واریز به حساب دفتر"
                QuickActionType.TRANSFER -> "انتقال به کارت/حساب"
                QuickActionType.SWAP -> "تبدیل دلار به طلا"
                QuickActionType.ANALYTICS -> "تحلیل سود و زیان"
            }
        )
    }

    var amountInput by remember { mutableStateOf("10000000") }
    var categoryInput by remember { mutableStateOf("عمومی") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (actionType) {
                    QuickActionType.DEPOSIT -> "ثبت جدید: واریز به دفتر"
                    QuickActionType.TRANSFER -> "ثبت جدید: انتقال مالی"
                    QuickActionType.SWAP -> "ثبت جدید: تبدیل دارایی"
                    QuickActionType.ANALYTICS -> "گزارش آنالیز دارایی"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("عنوان تراکنش") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("مبلغ (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_amount_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("دسته‌بندی (طلا / ارز / بورس / شخصی)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_category_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    val type = when (actionType) {
                        QuickActionType.DEPOSIT -> TransactionType.DEPOSIT
                        QuickActionType.TRANSFER -> TransactionType.TRANSFER
                        QuickActionType.SWAP -> TransactionType.SWAP
                        QuickActionType.ANALYTICS -> TransactionType.EXPENSE
                    }
                    onConfirmTransaction(titleInput, amount, type, categoryInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CredifyIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("ثبت در دفتر", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text("انصراف", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    )
}
