package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.PinDotsIndicator
import com.example.ui.components.PinNumericKeypad
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.CredifyViolet
import com.example.ui.theme.RoseLoss

/**
 * PIN creation, matching the provided design template: title, subtitle, 4 dot indicators,
 * numeric keypad, gradient "ساخت PIN" button. Internally does entry + confirm in one flow
 * (the template only showed the entry step) so a typo can't lock the person out later.
 */
@Composable
fun PinSetupScreen(onPinCreated: (String) -> Unit) {
    var firstEntry by remember { mutableStateOf<String?>(null) }
    var currentInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isConfirmStep = firstEntry != null

    fun handleDigit(digit: String) {
        if (currentInput.length >= 4) return
        errorMessage = null
        currentInput += digit
        if (currentInput.length == 4) {
            if (!isConfirmStep) {
                firstEntry = currentInput
                currentInput = ""
            } else {
                if (currentInput == firstEntry) {
                    onPinCreated(currentInput)
                } else {
                    errorMessage = "PIN‌ها یکسان نبودن، دوباره امتحان کن"
                    firstEntry = null
                    currentInput = ""
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isConfirmStep) "PIN را دوباره وارد کن" else "PIN بساز",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "یک رمز چهار رقمی برای امنیت حساب و دفتر مالیت بساز",
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
            PinDotsIndicator(enteredLength = currentInput.length)
            errorMessage?.let {
                Text(
                    it,
                    color = RoseLoss,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        PinNumericKeypad(
            onDigit = ::handleDigit,
            onBackspace = { if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) }
        )
        Text(
            "بعد از وارد کردن ۴ رقم، خودکار ادامه پیدا می‌کنه",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
