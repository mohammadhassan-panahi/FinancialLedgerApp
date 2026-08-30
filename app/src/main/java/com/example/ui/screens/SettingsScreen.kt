package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.SettingsViewModel

/**
 * Settings hub: currency display unit, PIN setup, biometric unlock, and backup/restore.
 * PinManager, BiometricAuthManager, SettingsViewModel and export/import launchers were
 * already built and passed down from MainActivity, but had no screen to live in — this
 * connects them.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricEnabled: Boolean,
    onOpenPinSetup: () -> Unit,
    onOpenBiometricEnable: () -> Unit,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit
) {
    val currencyUnit by viewModel.currencyUnit.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "تنظیمات و امنیت",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text("واحد نمایش مبالغ", fontWeight = FontWeight.SemiBold)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = currencyUnit == "TOMAN",
                            onClick = { viewModel.setCurrencyUnit("TOMAN") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("تومان") }
                        SegmentedButton(
                            selected = currencyUnit == "RIAL",
                            onClick = { viewModel.setCurrencyUnit("RIAL") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("ریال") }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpenPinSetup() }) {
                ListItem(
                    headlineContent = { Text("تنظیم رمز عبور (PIN)") },
                    supportingContent = { Text("قفل کردن برنامه با یک کد چهاررقمی") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpenBiometricEnable() }) {
                ListItem(
                    headlineContent = { Text(if (biometricEnabled) "اثر انگشت / چهره فعال است" else "فعال‌سازی ورود با اثر انگشت") },
                    supportingContent = { Text("باز کردن سریع‌تر برنامه با بیومتریک دستگاه") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().clickable { onExportRequested() }) {
                ListItem(
                    headlineContent = { Text("پشتیبان‌گیری") },
                    supportingContent = { Text("ذخیره‌ی تمام داده‌های مالی در یک فایل JSON") },
                    leadingContent = { Icon(Icons.Default.Backup, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().clickable { onImportRequested() }) {
                ListItem(
                    headlineContent = { Text("بازیابی از پشتیبان") },
                    supportingContent = { Text("بازگرداندن داده‌ها از یک فایل پشتیبان قبلی") },
                    leadingContent = { Icon(Icons.Default.Restore, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
