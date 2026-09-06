package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.LocalIsRial
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricEnabled: Boolean,
    onOpenPinSetup: () -> Unit,
    onOpenBiometricEnable: () -> Unit,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit,
    onBack: () -> Unit = {}
) {
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val isPrivacyModeEnabled by viewModel.isPrivacyModeEnabled.collectAsState()
    val isRial = currencyUnit == "RIAL"

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            SettingsHeader(onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // VIP Profile Card
            VipProfileCard()

            // Financial Unit
            SettingsSection(title = "واحد پول و محاسبات مالی", icon = Icons.Default.AccountBalanceWallet) {
                CurrencyToggle(
                    isRial = isRial,
                    onToggle = { viewModel.setCurrencyUnit(if (isRial) "TOMAN" else "RIAL") }
                )
            }

            // Security
            SettingsSection(title = "امنیت و کنترل دسترسی", icon = Icons.Default.Shield) {
                SettingsActionRow(
                    title = "قفل بیومتریک و چهره",
                    subtitle = "ورود سریع و امن با اثر انگشت",
                    icon = Icons.Default.Fingerprint,
                    color = EmeraldCore
                ) {
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { onOpenBiometricEnable() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldCore)
                    )
                }
                SettingsActionRow(
                    title = "رمز عبور و پین‌کد",
                    subtitle = "تغییر پین‌کد ورود ۴ رقمی",
                    icon = Icons.Default.Pin,
                    color = IndigoElectric
                ) {
                    IconButton(onClick = onOpenPinSetup) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Slate600)
                    }
                }
            }

            // Privacy
            SettingsSection(title = "حریم خصوصی", icon = Icons.Default.VisibilityOff) {
                SettingsActionRow(
                    title = "مخفی‌سازی موجودی",
                    subtitle = "پوشاندن مبالغ در صفحه اصلی",
                    icon = Icons.Default.Password,
                    color = RefinedAmberGold
                ) {
                    Switch(
                        checked = isPrivacyModeEnabled,
                        onCheckedChange = { viewModel.setPrivacyModeEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = RefinedAmberGold)
                    )
                }
            }

            // Data
            SettingsSection(title = "مدیریت داده‌ها", icon = Icons.Default.Storage) {
                SettingsActionRow(title = "پشتیبان‌گیری (Export)", icon = Icons.Default.CloudUpload, color = Slate400) {
                    TextButton(onClick = onExportRequested) { Text("اجرا", color = IndigoElectric) }
                }
                SettingsActionRow(title = "بازیابی داده‌ها (Import)", icon = Icons.Default.CloudDownload, color = Slate400) {
                    TextButton(onClick = onImportRequested) { Text("اجرا", color = IndigoElectric) }
                }
            }

            // Logout
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSlate800)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Logout, null, tint = RoseCoral)
                    Text("خروج امن از حساب کاربری", style = DaraTypography.labelLarge, color = Slate50, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate50)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("تنظیمات و امنیت", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VipProfileCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate800,
        border = BorderStroke(1.dp, RefinedAmberGold.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDlWqJtR7qE6yDwb5YoQy6JciiWLwRIPDT7p3HMc83WcubH55WAejDHs2EixhCX9lMv1VtwEtoxLpr7QGocN2w5Py8v6TYedBZDjMbP5sLn0inJz-xmA7_kBGdr9mnLe-Sclxf1S2xo9raBdwfcUcY1OxLZ95NC_5PyNUTULQ9Gq1igjkgkSPSEg6rnihzPbPhp9eBYsu9JqigB6hXdgQZ_liBPaPrv_Od1GQDdZuaGwJLoXM2Fs6YJ",
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("علیرضا کمالی", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Verified, null, tint = RefinedAmberGold, modifier = Modifier.size(16.dp))
                }
                Text("کاربر VIP دارا پرمیوم", style = DaraTypography.labelSmall, color = RefinedAmberGold)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = Slate600)
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = IndigoElectric, modifier = Modifier.size(18.dp))
            Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSlate800.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsActionRow(title: String, subtitle: String? = null, icon: ImageVector, color: Color, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(subtitle, style = DaraTypography.labelSmall, color = Slate600)
                }
            }
        }
        action()
    }
}

@Composable
fun CurrencyToggle(isRial: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        color = ObsidianSlate900,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            CurrencyButton(label = "تومان", isSelected = !isRial, onClick = onToggle, modifier = Modifier.weight(1f))
            CurrencyButton(label = "ریال", isSelected = isRial, onClick = onToggle, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CurrencyButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ObsidianSlate700 else Color.Transparent
    ) {
        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Text(label, style = DaraTypography.labelMedium, color = if (isSelected) Slate50 else Slate600, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
