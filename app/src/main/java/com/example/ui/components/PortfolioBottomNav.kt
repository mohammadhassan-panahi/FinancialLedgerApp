package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CredifyIndigo

enum class PortfolioTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "خانه", Icons.Default.Home),
    GOLD_DOLLAR("gold_dollar", "طلا و دلار", Icons.Default.MonetizationOn),
    STOCK("stock", "بورس", Icons.Default.ShowChart),
    ADD_PURCHASE("add_purchase", "افزودن", Icons.Default.Add)
}

@Composable
fun PortfolioBottomNav(currentTab: PortfolioTab, onTabSelected: (PortfolioTab) -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PortfolioTab.entries.forEach { tab ->
                val selected = tab == currentTab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) CredifyIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        color = if (selected) CredifyIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
