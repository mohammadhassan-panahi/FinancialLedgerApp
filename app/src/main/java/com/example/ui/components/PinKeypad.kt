package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.PersianNumberUtils

/**
 * Modern Dara PIN dots indicator with glowing effects.
 */
@Composable
fun PinDotsIndicator(enteredLength: Int, totalLength: Int = 4) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalLength) { index ->
            val filled = index < enteredLength
            Box(contentAlignment = Alignment.Center) {
                if (filled) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(IndigoElectric.copy(alpha = 0.4f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(IndigoElectric, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ObsidianSlate600, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(4.dp).background(Slate600, CircleShape))
                    }
                }
            }
        }
    }
}

/**
 * Dara Ergonomic Numeric Keypad.
 */
@Composable
fun PinNumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: (() -> Unit)? = null
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { digit ->
                    KeypadButton(
                        text = PersianNumberUtils.toPersianDigits(digit),
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            // Biometric or empty
            if (onBiometricClick != null) {
                KeypadIconButton(
                    icon = Icons.Default.Fingerprint,
                    label = "زیست‌سنجی",
                    color = EmeraldCore,
                    onClick = onBiometricClick
                )
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            // Zero
            KeypadButton(
                text = PersianNumberUtils.toPersianDigits("0"),
                onClick = { onDigit("0") }
            )

            // Backspace
            KeypadIconButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                label = "حذف",
                color = Slate400,
                onClick = onBackspace
            )
        }
    }
}

@Composable
private fun KeypadButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = ObsidianSlate700.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = DaraTypography.headlineLarge,
                color = Slate50,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun KeypadIconButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = ObsidianSlate700.copy(alpha = 0.4f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                style = DaraTypography.labelSmall,
                color = color,
                fontSize = 9.sp
            )
        }
    }
}
