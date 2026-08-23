package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.CredifyViolet

/**
 * Biometric opt-in, matching the provided design template: concentric rings around a
 * gradient fingerprint circle, title, subtitle, gradient "فعال کردن ورود بیومتریک" button,
 * and a plain "بعداً" skip link. Shown once, right after PIN setup, only on devices that
 * actually have biometrics/device-credential available.
 */
@Composable
fun BiometricEnableScreen(onEnable: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RingedFingerprintIcon()
        }

        Text(
            "ورود سریع‌تر با اثر انگشت",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "با فعال کردن اثر انگشت یا Face ID، سریع‌تر و امن‌تر به دفتر مالیت وارد شو",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Button(
            onClick = onEnable,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(CredifyIndigo, CredifyViolet)),
                    shape = RoundedCornerShape(28.dp)
                ),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text("فعال کردن ورود بیومتریک", color = Color.White, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onSkip, modifier = Modifier.padding(top = 8.dp)) {
            Text("بعداً از تنظیمات فعالش می‌کنم")
        }
    }
}

@Composable
private fun RingedFingerprintIcon() {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(CredifyIndigo.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(CredifyIndigo.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(listOf(CredifyViolet, CredifyIndigo))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}
