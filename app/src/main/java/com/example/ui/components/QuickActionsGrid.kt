package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.CredifyViolet
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent

enum class QuickActionType {
    DEPOSIT, TRANSFER, SWAP, ANALYTICS
}

@Composable
fun QuickActionsGrid(
    onActionSelected: (QuickActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            title = "واریز",
            icon = Icons.Default.AddCard,
            accentColor = CredifyIndigo,
            testTag = "action_deposit",
            onClick = { onActionSelected(QuickActionType.DEPOSIT) },
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "انتقال",
            icon = Icons.Default.Send,
            accentColor = CredifyViolet,
            testTag = "action_transfer",
            onClick = { onActionSelected(QuickActionType.TRANSFER) },
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "تبدیل",
            icon = Icons.Default.SwapHoriz,
            accentColor = CyanAccent,
            testTag = "action_swap",
            onClick = { onActionSelected(QuickActionType.SWAP) },
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "آنالیز",
            icon = Icons.Default.Analytics,
            accentColor = GoldAccent,
            testTag = "action_analytics",
            onClick = { onActionSelected(QuickActionType.ANALYTICS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
