package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.PortfolioAssetType
import com.example.ui.components.PersianNumberTextField
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianNumberUtils
import com.example.util.formatRial

@Composable
fun AddPurchaseScreen(viewModel: PortfolioViewModel) {
    var assetType by remember { mutableStateOf(PortfolioAssetType.GOLD) }
    var assetCode by remember { mutableStateOf("GOLD_18K") }
    var assetName by remember { mutableStateOf("طلا ۱۸ عیار (گرم)") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    val purchases by viewModel.purchases.collectAsStateWithLifecycle()

    fun selectType(type: PortfolioAssetType) {
        assetType = type
        when (type) {
            PortfolioAssetType.GOLD -> { assetCode = "GOLD_18K"; assetName = "طلا ۱۸ عیار (گرم)" }
            PortfolioAssetType.USD -> { assetCode = "USD"; assetName = "دلار آمریکا" }
            PortfolioAssetType.STOCK -> { assetCode = ""; assetName = "" }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("نوع دارایی", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                FilterChip(selected = assetType == PortfolioAssetType.GOLD, onClick = { selectType(PortfolioAssetType.GOLD) }, label = { Text("طلا") })
                FilterChip(selected = assetType == PortfolioAssetType.USD, onClick = { selectType(PortfolioAssetType.USD) }, label = { Text("دلار") })
                FilterChip(selected = assetType == PortfolioAssetType.STOCK, onClick = { selectType(PortfolioAssetType.STOCK) }, label = { Text("سهام") })
            }
        }
        if (assetType == PortfolioAssetType.STOCK) {
            item {
                androidx.compose.material3.OutlinedTextField(
                    value = assetCode,
                    onValueChange = { assetCode = it; assetName = it },
                    label = { Text("نماد بورسی (مثلاً فولاد)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            PersianNumberTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = when (assetType) {
                    PortfolioAssetType.STOCK -> "تعداد سهم"
                    PortfolioAssetType.GOLD -> "مقدار (گرم)"
                    PortfolioAssetType.USD -> "مقدار (دلار)"
                },
                isDecimalAllowed = true
            )
        }
        item {
            PersianNumberTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = "قیمت واحد (ریال)",
                suffix = "ریال"
            )
        }
        item {
            val q = PersianNumberUtils.parseAmount(quantity)
            val p = PersianNumberUtils.parseAmount(unitPrice)
            Text("مجموع: ${formatRial(q * p)}", fontWeight = FontWeight.Bold)
        }
        item {
            val q = PersianNumberUtils.parseAmount(quantity)
            val p = PersianNumberUtils.parseAmount(unitPrice)
            val canSubmit = q > 0 && p > 0 && assetCode.isNotBlank()
            Button(
                enabled = canSubmit,
                onClick = {
                    viewModel.addPurchase(
                        assetType = assetType,
                        assetCode = assetCode,
                        assetName = assetName.ifBlank { assetCode },
                        quantity = q,
                        unitPriceRial = p,
                        purchaseDate = System.currentTimeMillis()
                    )
                    quantity = ""
                    unitPrice = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ثبت تراکنش")
            }
        }
        item {
            Text(
                "تاریخچه خریدها",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(purchases) { purchase ->
            PurchaseRow(purchase, onDelete = { viewModel.deletePurchase(purchase.id) })
        }
    }
}

@Composable
private fun PurchaseRow(purchase: AssetPurchaseEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(purchase.assetName, fontWeight = FontWeight.Bold)
                Text(formatRial(purchase.totalPaidRial), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "حذف",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}
