package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.AccentGold
import com.example.util.PersianNumberUtils

@Composable
fun PersianNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    isDecimalAllowed: Boolean = false,
    imeAction: ImeAction = ImeAction.Next
) {
    // Keep raw numeric string in parent state
    // Format display text with thousands commas
    val displayString = remember(value) {
        if (value.isBlank()) "" else PersianNumberUtils.formatFormattedInput(value)
    }

    var textFieldValueState by remember(displayString) {
        mutableStateOf(
            TextFieldValue(
                text = displayString,
                selection = TextRange(displayString.length)
            )
        )
    }

    OutlinedTextField(
        value = textFieldValueState,
        onValueChange = { newValue ->
            val cleanEnglish = PersianNumberUtils.toEnglishDigits(newValue.text)
                .replace(",", "")
                .replace(" ", "")

            val filtered = if (isDecimalAllowed) {
                cleanEnglish.filterIndexed { index, c ->
                    c.isDigit() || (c == '.' && cleanEnglish.indexOf('.') == index)
                }
            } else {
                cleanEnglish.filter { it.isDigit() }
            }

            onValueChange(filtered)

            val formattedNew = PersianNumberUtils.formatFormattedInput(filtered)
            textFieldValueState = newValue.copy(
                text = formattedNew,
                selection = TextRange(formattedNew.length)
            )
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimalAllowed) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentGold,
            focusedLabelColor = AccentGold
        ),
        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Start),
        modifier = modifier.fillMaxWidth()
    )
}
