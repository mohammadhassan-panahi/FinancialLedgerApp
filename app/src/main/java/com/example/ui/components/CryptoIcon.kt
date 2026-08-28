package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CryptoIcon(
    cmcId: Int?,
    symbol: String,
    size: Dp = 40.dp
) {
    if (cmcId != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://s2.coinmarketcap.com/static/img/coins/64x64/$cmcId.png")
                .crossfade(true)
                .build(),
            contentDescription = symbol,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Fit
        )
    } else {
        // Fallback if ID is missing: show first letter of symbol
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol.take(1),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = (size.value / 2.5).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
