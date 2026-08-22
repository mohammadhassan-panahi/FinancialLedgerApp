package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CredifyIndigo

/** Four dot/box indicators showing how many of the 4 PIN digits have been entered — matches the design template. */
@Composable
fun PinDotsIndicator(enteredLength: Int, totalLength: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(totalLength) { index ->
            val filled = index < enteredLength
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = if (filled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CredifyIndigo, CircleShape)
                    )
                }
            }
        }
    }
}

/** Numeric 1-9,0,backspace keypad — matches the design template's simple 3-column grid. */
@Composable
fun PinNumericKeypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                row.forEach { digit -> KeypadKey(digit) { onDigit(digit) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Box(modifier = Modifier.size(64.dp))
            KeypadKey("0") { onDigit("0") }
            Box(
                modifier = Modifier.size(64.dp).clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "پاک کردن")
            }
        }
    }
}

@Composable
private fun KeypadKey(digit: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(64.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(digit, fontSize = 26.sp, fontWeight = FontWeight.Medium)
    }
}
