package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.local.AlertDirection
import com.example.util.PersianNumberUtils

/**
 * Small dialog for setting a price alert on one asset. Direction (ABOVE/BELOW) is inferred
 * automatically from whether the target the user types is above or below the current price.
 */
@Composable
fun PriceAlertDialog(
    assetName: String,
    currentPriceRial: Double,
    onDismiss: () -> Unit,
    onConfirm: (targetPriceRial: Double, direction: AlertDirection) -> Unit
) {
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("هشدار قیمت برای $assetName") },
        text = {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("قیمت هدف (ریال)") }
            )
        },
        confirmButton = {
            Button(onClick = {
                val t = PersianNumberUtils.parseAmount(target)
                if (t > 0) {
                    val direction = if (t >= currentPriceRial) AlertDirection.ABOVE else AlertDirection.BELOW
                    onConfirm(t, direction)
                }
            }) { Text("ثبت هشدار") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
