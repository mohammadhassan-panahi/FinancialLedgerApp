package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionType
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.CredifyViolet
import com.example.ui.theme.RoseLoss
import com.example.ui.viewmodel.MarketPortfolioViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: MarketPortfolioViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val availableBalance by viewModel.portfolioBalance.collectAsStateWithLifecycle()

    var amountInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var sourceAccount by remember { mutableStateOf("حساب اصلی پاسارگاد") }
    var targetAccount by remember { mutableStateOf("صندوق سرمایه‌گذاری واعظ") }
    var selectedCategory by remember { mutableStateOf("انتقال بین‌بانکی") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accounts = listOf(
        "حساب اصلی پاسارگاد",
        "حساب پس‌انداز بانک ملی",
        "صندوق سرمایه‌گذاری واعظ",
        "حساب کارگزاری مفید",
        "کیف پول نقدینگی دفتر"
    )

    val categories = listOf(
        "انتقال بین‌بانکی",
        "شارژ حساب کارگزاری",
        "جابجایی پس‌انداز",
        "انتقال به صندوق NAV",
        "انتقال متفرقه"
    )

    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val sanitizedAmount = amountInput.filter { it.isDigit() }
    val parsedAmount = sanitizedAmount.toDoubleOrNull() ?: 0.0
    val formattedToman = if (parsedAmount > 0) NumberFormat.getNumberInstance(Locale.US).format(parsedAmount.toLong()) else "۰"
    val formattedBalance = NumberFormat.getNumberInstance(Locale.US).format(availableBalance.toLong())

    val isInsufficientBalance = parsedAmount > availableBalance

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "انتقال بین حساب‌ها و دفتر",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_transfer")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize().testTag("transfer_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Balance Overview Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CredifyViolet.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = CredifyViolet,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "موجودی قابل انتقال دفتر",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$formattedBalance تومان",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Source & Target Account Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Source Dropdown
                    ExposedDropdownMenuBox(
                        expanded = sourceExpanded,
                        onExpandedChange = { sourceExpanded = !sourceExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = sourceAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("از حساب (مبداء)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CredifyIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("dropdown_transfer_source")
                        )
                        ExposedDropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        sourceAccount = acc
                                        sourceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Transfer Direction",
                            tint = CredifyIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Dropdown
                    ExposedDropdownMenuBox(
                        expanded = targetExpanded,
                        onExpandedChange = { targetExpanded = !targetExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = targetAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("به حساب (مقصد)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CredifyIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("dropdown_transfer_target")
                        )
                        ExposedDropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        targetAccount = acc
                                        targetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Amount Input Field
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مبلغ انتقال (تومان)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 12) {
                                amountInput = digits
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("مثلاً ۲,۵۰۰,۰۰۰ تومان", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CredifyViolet,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_transfer_amount")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "معادل: $formattedToman تومان",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isInsufficientBalance) RoseLoss else CredifyViolet,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isInsufficientBalance) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "خطا: مبلغ درخواستی بیش از موجودی کل دفتر ($formattedBalance تومان) می‌باشد.",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RoseLoss,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Category Selector
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("دسته‌بندی انتقال", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CredifyIndigo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_transfer_category")
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("عنوان انتقال (اختیاری)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                placeholder = { Text("مثلاً انتقال جهت خرید واحدهای صندوق", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CredifyIndigo,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_transfer_title")
            )

            errorMessage?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Button
            Button(
                onClick = {
                    if (parsedAmount <= 0.0) {
                        errorMessage = "لطفاً مبلغ معتبری بیشتر از صفر وارد نمایید."
                        return@Button
                    }
                    if (isInsufficientBalance) {
                        errorMessage = "موجودی حساب جهت انجام این انتقال کافی نیست."
                        return@Button
                    }

                    val finalTitle = titleInput.trim().ifEmpty { "$sourceAccount ⬅️ $targetAccount" }
                    viewModel.addTransaction(
                        title = finalTitle,
                        amount = parsedAmount,
                        type = TransactionType.TRANSFER,
                        category = selectedCategory
                    )

                    Toast.makeText(context, "انتقال $formattedToman تومان با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                enabled = !isInsufficientBalance && parsedAmount > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CredifyViolet),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("button_submit_transfer")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تأیید و اجرای انتقال بین حساب‌ها",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
