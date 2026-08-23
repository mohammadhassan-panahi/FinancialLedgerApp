package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.PinDotsIndicator
import com.example.ui.components.PinNumericKeypad
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.RoseLoss

/**
 * PIN entry for every app open after the first. If biometrics are enabled, offers a
 * fingerprint shortcut that triggers the system prompt instead of typing the PIN.
 */
@Composable
fun PinEntryScreen(
    biometricEnabled: Boolean,
    onVerifyPin: (String) -> Boolean,
    onUnlocked: () -> Unit,
    onBiometricRequested: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (biometricEnabled) onBiometricRequested()
    }

    fun handleDigit(digit: String) {
        if (input.length >= 4) return
        errorMessage = null
        input += digit
        if (input.length == 4) {
            if (onVerifyPin(input)) {
                onUnlocked()
            } else {
                errorMessage = "PIN اشتباه است"
                input = ""
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 64.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("دفتر مالی قفل است", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "PIN چهار رقمی‌ات را وارد کن",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinDotsIndicator(enteredLength = input.length)
            errorMessage?.let {
                Text(it, color = RoseLoss, modifier = Modifier.padding(top = 16.dp))
            }
        }

        PinNumericKeypad(
            onDigit = ::handleDigit,
            onBackspace = { if (input.isNotEmpty()) input = input.dropLast(1) }
        )

        if (biometricEnabled) {
            Row(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable { onBiometricRequested() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CredifyIndigo)
                Text(
                    "ورود با اثر انگشت",
                    color = CredifyIndigo,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
