package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PortfolioAssetType
import com.example.ui.LocalIsRial
import com.example.ui.components.DaraGlassCard
import com.example.ui.components.PersianNumberTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.util.PersianNumberUtils
import com.example.util.formatRial
import java.math.BigDecimal

@Composable
fun AddPurchaseScreen(
    viewModel: PortfolioViewModel,
    onNextStep: (com.example.data.local.PortfolioAssetType) -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedType by remember { mutableStateOf<com.example.data.local.PortfolioAssetType?>(null) }
    
    // State for Step 2
    var assetCode by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    val allMarketRates by viewModel.marketRates.collectAsStateWithLifecycle()
    val cryptoAssets by viewModel.cryptoAssets.collectAsStateWithLifecycle()
    val mutualFunds by viewModel.mutualFunds.collectAsStateWithLifecycle()

    val marketRates = allMarketRates.filter { !it.isOfflineRate || it.currency == "تومان" }
    val usdRateToman = marketRates.find { it.assetCode == "USD" }?.priceToman ?: BigDecimal("65000")

    fun autoFillPrice() {
        val type = selectedType ?: return
        val rialPerToman = BigDecimal("10")
        val livePriceRial = when (type) {
            PortfolioAssetType.GOLD -> {
                val rate = marketRates.find { it.assetCode == assetCode || it.name.contains(assetName) }
                rate?.priceToman?.let { it.multiply(rialPerToman) }
            }
            PortfolioAssetType.USD -> {
                val rate = marketRates.find { it.assetCode == assetCode || it.assetCode == "USD" }
                rate?.priceToman?.let { it.multiply(rialPerToman) }
            }
            PortfolioAssetType.CRYPTO -> cryptoAssets.find { it.symbol == assetCode }?.priceUsd?.let { it.multiply(usdRateToman).multiply(rialPerToman) }
            PortfolioAssetType.FUND -> mutualFunds.find { it.id == assetCode }?.navToman?.let { it.multiply(rialPerToman) }
            else -> null
        }
        if (livePriceRial != null) unitPrice = livePriceRial.toLong().toString()
    }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            AddAssetHeader(
                currentStep = currentStep,
                onBack = { if (currentStep > 1) currentStep-- }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    } else {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                if (step == 1) {
                    AssetTypeSelectionStep(
                        selectedType = selectedType,
                        onTypeSelected = { 
                            selectedType = it
                            onNextStep(it)
                        },
                        onContinue = { currentStep = 2 }
                    )
                } else {
                    AssetDetailsFormStep(
                        assetType = selectedType!!,
                        assetCode = assetCode,
                        onAssetCodeChange = { assetCode = it; assetName = it },
                        assetName = assetName,
                        quantity = quantity,
                        onQuantityChange = { quantity = it },
                        unitPrice = unitPrice,
                        onUnitPriceChange = { unitPrice = it },
                        onAutoFill = { autoFillPrice() },
                        onSubmit = {
                            val q = PersianNumberUtils.parseAmount(quantity)
                            val p = PersianNumberUtils.parseAmount(unitPrice)
                            viewModel.addPurchase(
                                assetType = selectedType!!,
                                assetCode = assetCode,
                                assetName = assetName,
                                quantity = q,
                                unitPriceRial = p,
                                purchaseDate = System.currentTimeMillis()
                            )
                            // Reset and go back or show success
                            currentStep = 1
                            quantity = ""
                            unitPrice = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddAssetHeader(currentStep: Int, onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack, enabled = currentStep > 1) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (currentStep > 1) Slate50 else Slate600)
                }
                Column {
                    Text("ثبت دارایی جدید", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.width(if (currentStep == 1) 20.dp else 10.dp).height(4.dp).clip(CircleShape).background(if (currentStep == 1) IndigoElectric else Slate600))
                        Box(modifier = Modifier.width(if (currentStep == 2) 20.dp else 10.dp).height(4.dp).clip(CircleShape).background(if (currentStep == 2) IndigoElectric else Slate600))
                        Text("گام $currentStep از ۲", style = DaraTypography.labelSmall, color = Slate400)
                    }
                }
            }
            IconButton(onClick = { /* Close */ }) {
                Icon(Icons.Default.Close, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun AssetTypeSelectionStep(
    selectedType: PortfolioAssetType?,
    onTypeSelected: (PortfolioAssetType) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("چه دارایی جدیدی ثبت می‌کنید؟", style = DaraTypography.headlineLarge, color = Slate50)
        
        // Search Bar Placeholder
        DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Search, null, tint = Slate400)
                Text("جستجوی نماد، ارز یا طلا...", style = DaraTypography.bodyMedium, color = Slate600)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AssetTypeCard(
                    type = PortfolioAssetType.GOLD,
                    title = "طلا، مسکوکات و آبشده",
                    desc = "طلای ۱۸ و ۲۴ عیار، سکه امامی، نیم و ربع",
                    icon = Icons.Default.MonetizationOn,
                    color = RefinedAmberGold,
                    isSelected = selectedType == PortfolioAssetType.GOLD,
                    onClick = { onTypeSelected(PortfolioAssetType.GOLD) }
                )
            }
            item {
                AssetTypeCard(
                    type = PortfolioAssetType.USD,
                    title = "ارز نقدی و اسکناس",
                    desc = "دلار، یورو، درهم و سایر اسعار بازار آزاد",
                    icon = Icons.Default.Payments,
                    color = EmeraldCore,
                    isSelected = selectedType == PortfolioAssetType.USD,
                    onClick = { onTypeSelected(PortfolioAssetType.USD) }
                )
            }
            item {
                AssetTypeCard(
                    type = PortfolioAssetType.CRYPTO,
                    title = "رمزارز و استیبل‌کوین",
                    desc = "تتر، بیت‌کوین، اتریوم و دارایی‌های دیجیتال",
                    icon = Icons.Default.CurrencyBitcoin,
                    color = IndigoElectric,
                    isSelected = selectedType == PortfolioAssetType.CRYPTO,
                    onClick = { onTypeSelected(PortfolioAssetType.CRYPTO) }
                )
            }
            item {
                AssetTypeCard(
                    type = PortfolioAssetType.STOCK,
                    title = "بورس و فرابورس ایران",
                    desc = "سهام، صندوق‌های اهرمی و اوراق بهادار",
                    icon = Icons.Default.QueryStats,
                    color = Color(0xFF8B5CF6),
                    isSelected = selectedType == PortfolioAssetType.STOCK,
                    onClick = { onTypeSelected(PortfolioAssetType.STOCK) }
                )
            }
        }

        Button(
            onClick = onContinue,
            enabled = selectedType != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ادامه و ورود جزئیات", style = DaraTypography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AssetTypeCard(
    type: PortfolioAssetType,
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ObsidianSlate600 else ObsidianSlate800,
        border = BorderStroke(1.dp, if (isSelected) IndigoElectric else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = DaraTypography.titleMedium, color = if (isSelected) color else Slate50, fontWeight = FontWeight.Bold)
                Text(desc, style = DaraTypography.bodySmall, color = Slate400, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AssetDetailsFormStep(
    assetType: PortfolioAssetType,
    assetCode: String,
    onAssetCodeChange: (String) -> Unit,
    assetName: String,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    unitPrice: String,
    onUnitPriceChange: (String) -> Unit,
    onAutoFill: () -> Unit,
    onSubmit: () -> Unit
) {
    val isRial = LocalIsRial.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("جزئیات خرید ${assetType.name}", style = DaraTypography.headlineLarge, color = Slate50)

        DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Asset Search/Code
                OutlinedTextField(
                    value = assetCode,
                    onValueChange = onAssetCodeChange,
                    label = { Text("نام یا نماد دارایی") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate50,
                        unfocusedTextColor = Slate50,
                        focusedBorderColor = IndigoElectric,
                        unfocusedBorderColor = ObsidianSlate600
                    )
                )

                // Quantity
                PersianNumberTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = "مقدار / تعداد",
                    isDecimalAllowed = true
                )

                // Unit Price
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("قیمت واحد (خرید)", style = DaraTypography.labelMedium, color = Slate400)
                        TextButton(onClick = onAutoFill, contentPadding = PaddingValues(0.dp)) {
                            Icon(Icons.Default.AutoAwesome, null, tint = EmeraldCore, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("قیمت زنده", style = DaraTypography.labelSmall, color = EmeraldCore)
                        }
                    }
                    PersianNumberTextField(
                        value = unitPrice,
                        onValueChange = onUnitPriceChange,
                        label = "قیمت به ریال",
                        suffix = "ریال"
                    )
                }

                // Total Calculation
                val q = PersianNumberUtils.parseAmount(quantity)
                val p = PersianNumberUtils.parseAmount(unitPrice)
                val total = q.multiply(p)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSlate600.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مجموع پرداخت شده:", style = DaraTypography.bodyMedium, color = Slate400)
                        Text(formatRial(total, isRial = isRial), style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onSubmit,
                    enabled = q.compareTo(BigDecimal.ZERO) > 0 && p.compareTo(BigDecimal.ZERO) > 0 && assetCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric)
                ) {
                    Text("ثبت در پورتفوی دارا", style = DaraTypography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // AI Advice box
        DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Info, null, tint = RefinedAmberGold, modifier = Modifier.size(20.dp))
                Text(
                    "با ثبت دقیق تاریخ خرید، دارا می‌تواند نرخ تورم را از سود شما کسر کرده و سود واقعی (Real Return) را محاسبه کند.",
                    style = DaraTypography.bodySmall,
                    color = Slate400,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
