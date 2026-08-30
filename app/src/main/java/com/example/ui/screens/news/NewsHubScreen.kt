package com.example.ui.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.NewsEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.NewsViewModel
import com.example.util.PersianDateUtils

@Composable
fun NewsHubScreen(viewModel: NewsViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("اخبار ایران", "اخبار کریپتو")

    val iranEconomyNews by viewModel.iranEconomyNews.collectAsStateWithLifecycle()
    val cryptoNews by viewModel.cryptoNews.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (cryptoNews.isEmpty() && iranEconomyNews.isEmpty()) {
            viewModel.refreshNews()
        }
    }

    Scaffold(
        containerColor = DarkSlateSurface,
        topBar = {
            Column(modifier = Modifier.background(DarkSlateSurface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📰 پیشخوان اخبار", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = { viewModel.refreshNews() }, enabled = !isRefreshing) {
                        Icon(Icons.Default.Refresh, null, tint = CredifyIndigo)
                    }
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CredifyIndigo,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CredifyIndigo,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val newsList = if (selectedTab == 0) iranEconomyNews else cryptoNews

        if (newsList.isEmpty() && isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CredifyIndigo)
            }
        } else if (newsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("خبری برای نمایش وجود ندارد", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(newsList) { news ->
                    NewsCard(news)
                }
            }
        }
    }
}

@Composable
fun NewsCard(news: NewsEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* Open URL */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column {
            if (news.imageUrl != null) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ImportanceBadge(news.importance)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(news.source, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(PersianDateUtils.formatRelativeTime(news.publishedAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (news.category == "CRYPTO") {
                    Surface(
                        color = CredifyIndigo.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ترجمه‌شده با هوش مصنوعی — عنوان اصلی: ${news.description ?: news.title}",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (news.aiSummary != null) {
                    Surface(
                        color = CredifyIndigo.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "خلاصه دارا: ${news.aiSummary}",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SentimentBadge(news.sentiment)
                    news.relatedAssets?.split(",")?.take(3)?.forEach { asset ->
                        AssistChip(
                            onClick = {},
                            label = { Text(asset, fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImportanceBadge(importance: String) {
    val (color, text) = when (importance) {
        "HIGH" -> RoseLoss to "فوری"
        "LOW" -> EmeraldProfit to "عادی"
        else -> GoldAccent to "مهم"
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
        Text(text, color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SentimentBadge(sentiment: String) {
    val (color, icon) = when (sentiment) {
        "POSITIVE" -> EmeraldProfit to Icons.AutoMirrored.Filled.TrendingUp
        "NEGATIVE" -> RoseLoss to Icons.AutoMirrored.Filled.TrendingUp // TODO: Change icon
        else -> TextMuted to Icons.AutoMirrored.Filled.TrendingUp
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = when(sentiment) {
                "POSITIVE" -> "مثبت"
                "NEGATIVE" -> "منفی"
                else -> "خنثی"
            },
            color = color,
            fontSize = 11.sp
        )
    }
}
