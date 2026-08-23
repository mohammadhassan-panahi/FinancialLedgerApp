package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.GoldAccent

enum class BottomTab {
    PORTFOLIO, TRANSACTIONS, CALCULATOR, SECURITY
}

@Composable
fun FloatingBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("floating_bottom_bar"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = DarkSlateSurface,
            shadowElevation = 12.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "بازار",
                    icon = Icons.Default.PieChart,
                    isSelected = currentTab == BottomTab.PORTFOLIO,
                    testTag = "tab_portfolio",
                    onClick = { onTabSelected(BottomTab.PORTFOLIO) }
                )
                BottomNavItem(
                    label = "دفتر",
                    icon = Icons.Default.ReceiptLong,
                    isSelected = currentTab == BottomTab.TRANSACTIONS,
                    testTag = "tab_transactions",
                    onClick = { onTabSelected(BottomTab.TRANSACTIONS) }
                )
                BottomNavItem(
                    label = "محاسبه",
                    icon = Icons.Default.Calculate,
                    isSelected = currentTab == BottomTab.CALCULATOR,
                    testTag = "tab_calculator",
                    onClick = { onTabSelected(BottomTab.CALCULATOR) }
                )
                BottomNavItem(
                    label = "امنیت",
                    icon = Icons.Default.Lock,
                    isSelected = currentTab == BottomTab.SECURITY,
                    testTag = "tab_security",
                    onClick = { onTabSelected(BottomTab.SECURITY) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "icon_color"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) CredifyIndigo.copy(alpha = 0.35f) else Color.Transparent,
        animationSpec = tween(300),
        label = "pill_bg"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillBackground)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = iconColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
