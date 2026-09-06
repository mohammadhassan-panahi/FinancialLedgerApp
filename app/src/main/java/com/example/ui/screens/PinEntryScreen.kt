package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PinDotsIndicator
import com.example.ui.components.PinNumericKeypad
import com.example.ui.theme.*

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSlate900)
    ) {
        // Ambient background simulation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            IndigoElectric.copy(alpha = 0.05f),
                            Color.Transparent,
                            EmeraldCore.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // Security Shield
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSlate600,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = IndigoElectric,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = ObsidianSlate600.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Lock, null, tint = EmeraldCore, modifier = Modifier.size(14.dp))
                        Text(
                            "رمزنگاری سخت‌افزاری AES-256",
                            style = DaraTypography.labelSmall,
                            color = Slate400
                        )
                    }
                }
            }

            // User Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDlWqJtR7qE6yDwb5YoQy6JciiWLwRIPDT7p3HMc83WcubH55WAejDHs2EixhCX9lMv1VtwEtoxLpr7QGocN2w5Py8v6TYedBZDjMbP5sLn0inJz-xmA7_kBGdr9mnLe-Sclxf1S2xo9raBdwfcUcY1OxLZ95NC_5PyNUTULQ9Gq1igjkgkSPSEg6rnihzPbPhp9eBYsu9JqigB6hXdgQZ_liBPaPrv_Od1GQDdZuaGwJLoXM2Fs6YJ",
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, IndigoElectric, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(EmeraldCore, CircleShape)
                            .border(2.dp, ObsidianSlate900, CircleShape)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "خوش آمدید، سهراب عزیز",
                        style = DaraTypography.headlineSmall,
                        color = Slate50,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "رمز عبور ۴ رقمی خود را وارد نمایید",
                        style = DaraTypography.bodySmall,
                        color = Slate400,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // PIN Dots
            PinDotsIndicator(enteredLength = input.length)
            
            errorMessage?.let {
                Text(
                    text = it,
                    color = DaraError,
                    style = DaraTypography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Keypad
            PinNumericKeypad(
                onDigit = ::handleDigit,
                onBackspace = { if (input.isNotEmpty()) input = input.dropLast(1) },
                onBiometricClick = if (biometricEnabled) onBiometricRequested else null
            )

            // Recovery
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onClick = { /* Forgot PIN */ }) {
                    Text("رمز عبور را فراموش کرده‌اید؟", color = IndigoElectric, style = DaraTypography.labelLarge)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianSlate600)
                    Text("یا", style = DaraTypography.labelSmall, color = Slate600)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianSlate600)
                }

                Surface(
                    onClick = { /* SMS Login */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSlate600.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Sms, null, tint = RefinedAmberGold, modifier = Modifier.size(18.dp))
                        Text("ورود از طریق پیامک تایید", style = DaraTypography.labelLarge, color = Slate50)
                    }
                }
            }
        }
    }
}
