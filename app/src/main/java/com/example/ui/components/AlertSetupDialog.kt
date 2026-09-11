package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AlertDirection
import com.example.data.local.PriceAlertEntity
import com.example.ui.theme.Slate400
import java.math.BigDecimal

@Composable
fun AlertSetupDialog(
    assetName: String,
    assetCode: String,
    currentPriceRial: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (PriceAlertEntity) -> Unit
) {
    var alertType by remember { mutableIntStateOf(0) } // 0: Price, 1: Percentage
    var priceInput by remember { mutableStateOf("") }
    var percentInput by remember { mutableStateOf("5") }
    var direction by remember { mutableStateOf(AlertDirection.ABOVE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("تنظیم هشدار برای $assetName", fontWeight = FontWeight.Bold)
                Text("قیمت فعلی: ${com.example.util.formatRial(currentPriceRial, isRial = true)}", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Type Selector
                TabRow(selectedTabIndex = alertType) {
                    Tab(selected = alertType == 0, onClick = { alertType = 0 }) {
                        Text("بر اساس قیمت", modifier = Modifier.padding(12.dp))
                    }
                    Tab(selected = alertType == 1, onClick = { alertType = 1 }) {
                        Text("بر اساس درصد", modifier = Modifier.padding(12.dp))
                    }
                }

                if (alertType == 0) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("قیمت هدف (ریال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = direction == AlertDirection.ABOVE,
                            onClick = { direction = AlertDirection.ABOVE },
                            label = { Text("بیشتر از") }
                        )
                        FilterChip(
                            selected = direction == AlertDirection.BELOW,
                            onClick = { direction = AlertDirection.BELOW },
                            label = { Text("کمتر از") }
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = percentInput,
                        onValueChange = { percentInput = it },
                        label = { Text("درصد تغییر (۲۴ ساعت)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("٪") }
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = direction == AlertDirection.UP_PERCENT,
                            onClick = { direction = AlertDirection.UP_PERCENT },
                            label = { Text("افزایش") }
                        )
                        FilterChip(
                            selected = direction == AlertDirection.DOWN_PERCENT,
                            onClick = { direction = AlertDirection.DOWN_PERCENT },
                            label = { Text("کاهش") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val alert = if (alertType == 0) {
                    PriceAlertEntity(
                        assetCode = assetCode,
                        assetName = assetName,
                        targetPriceRial = priceInput.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        direction = direction
                    )
                } else {
                    PriceAlertEntity(
                        assetCode = assetCode,
                        assetName = assetName,
                        thresholdPercent = percentInput.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        direction = direction
                    )
                }
                onConfirm(alert)
            }) {
                Text("ثبت هشدار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
