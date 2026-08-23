package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.example.data.repository.HoldingSummary
import com.example.util.PersianNumberUtils
import com.example.util.formatRial

/**
 * Sells some quantity of an existing holding. Pre-fills the sale price with the current
 * market price (editable) and the quantity with the full holding (editable, for partial sells).
 */
@Composable
fun SellAssetDialog(
    holding: HoldingSummary,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (quantitySold: Double, saleUnitPriceRial: Double) -> Unit
) {
    var quantityText by remember { mutableStateOf(formatRial(holding.quantity, showSuffix = false, decimalPlaces = 2)) }
    var priceText by remember { mutableStateOf(formatRial(holding.currentPriceRial, showSuffix = false)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فروش ${holding.assetName}") },
        text = {
            Column {
                Text(
                    "موجودی فعلی: ${formatRial(holding.quantity, showSuffix = false, decimalPlaces = 2)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("مقدار فروش") },
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("قیمت واحد فروش (ریال)") },
                    modifier = Modifier.padding(top = 8.dp)
                )
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = PersianNumberUtils.parseAmount(quantityText)
                val p = PersianNumberUtils.parseAmount(priceText)
                if (q > 0 && p > 0) onConfirm(q, p)
            }) { Text("ثبت فروش") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
