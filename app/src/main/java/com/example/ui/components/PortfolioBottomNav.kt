package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.CredifyViolet
import com.example.ui.theme.DarkSlateSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class PortfolioTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "خانه", Icons.Default.Home),
    GOLD_DOLLAR("gold_dollar", "طلا و ارز", Icons.Default.CurrencyExchange),
    STOCK("stock", "بورس", Icons.Default.TrendingUp),
    CRYPTO("crypto", "کریپتو", Icons.Default.CurrencyBitcoin),
    ADD_PURCHASE("add_purchase", "افزودن", Icons.Default.Add)
}

@Composable
fun PortfolioBottomNav(currentTab: PortfolioTab, onTabSelected: (PortfolioTab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(36.dp),
            color = DarkSlateSecondary.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PortfolioTab.entries.forEach { tab ->
                    val selected = tab == currentTab
                    
                    val iconColor by animateColorAsState(
                        targetValue = if (selected) Color.White else TextSecondary,
                        animationSpec = tween(300)
                    )
                    
                    val backgroundBrush = if (selected) {
                        Brush.linearGradient(listOf(CredifyIndigo, CredifyViolet))
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }

                    Box(
                        modifier = Modifier
                            .size(if (tab == PortfolioTab.ADD_PURCHASE) 56.dp else 48.dp)
                            .clip(CircleShape)
                            .background(backgroundBrush)
                            .clickable { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(if (tab == PortfolioTab.ADD_PURCHASE) 28.dp else 24.dp)
                        )
                    }
                }
            }
        }
    }
}
