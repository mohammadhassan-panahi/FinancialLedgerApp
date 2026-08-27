package com.example.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SocialPostEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocialHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialHubScreen(viewModel: SocialHubViewModel) {
    val feed by viewModel.socialFeed.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("هاب اجتماعی نکس‌فین", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateSurface)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Show post dialog */ },
                    containerColor = CredifyIndigo
                ) {
                    Icon(Icons.Default.Add, contentDescription = "پست جدید", tint = Color.White)
                }
            },
            containerColor = DarkSlateSurface
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "تحلیل‌های اخیر جامعه",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }

                items(feed) { post ->
                    SocialPostCard(post)
                }
            }
        }
    }
}

@Composable
fun SocialPostCard(post: SocialPostEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(post.authorName, fontWeight = FontWeight.Bold, color = TextPrimary)
                post.assetCode?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CredifyIndigo.copy(alpha = 0.1f)
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            color = CredifyIndigo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.likesCount} لایک", color = TextSecondary, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.weight(1f))
                
                post.sentiment?.let {
                    Text(
                        if (it == "Bullish") "صعودی 📈" else "نزولی 📉",
                        color = if (it == "Bullish") EmeraldProfit else RoseLoss,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
