package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.util.FinancialFormulas
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanCalculatorCard(
    modifier: Modifier = Modifier
) {
    var principalInput by remember { mutableStateOf("50000000") }
    var rateInput by remember { mutableStateOf("23") }
    var monthsInput by remember { mutableStateOf("12") }

    var calculatedInstallment by remember { mutableStateOf<Double?>(null) }
    var calculatedTotalInterest by remember { mutableStateOf<Double?>(null) }

    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("loan_calculator_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = CredifyIndigo,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "محاسبه‌گر اقساط و سود تسهیلات بانک",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = principalInput,
                onValueChange = { principalInput = it },
                label = { Text("مبلغ تسهیلات (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_principal"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CredifyIndigo
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("سود سالانه (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_rate"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CredifyIndigo
                    )
                )

                OutlinedTextField(
                    value = monthsInput,
                    onValueChange = { monthsInput = it },
                    label = { Text("تعداد ماه") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_months"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CredifyIndigo
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val p = principalInput.toDoubleOrNull() ?: 0.0
                    val r = rateInput.toDoubleOrNull() ?: 0.0
                    val m = monthsInput.toIntOrNull() ?: 0
                    val monthly = FinancialFormulas.calculateLoanInstallment(p, r, m)
                    calculatedInstallment = monthly
                    calculatedTotalInterest = (monthly * m) - p
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_calculate_loan"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CredifyIndigo
                )
            ) {
                Text(
                    text = "محاسبه قسط ماهیانه",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            calculatedInstallment?.let { installment ->
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldProfit.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مبلغ هر قسط:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatter.format(installment.toLong())} تومان",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldProfit,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        calculatedTotalInterest?.let { interest ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "کل سود تسهیلات:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatter.format(interest.coerceAtLeast(0.0).toLong())} تومان",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = GoldAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
