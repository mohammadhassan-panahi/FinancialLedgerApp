package com.example.ui.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
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
import com.example.ui.components.DaraNewsRadarEmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.NewsViewModel
import com.example.util.PersianDateUtils

@Composable
fun NewsHubScreen(
    viewModel: NewsViewModel,
    onNewsClick: (NewsEntity) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("اقتصاد ایران", "رمزارز", "تکنولوژی")

    val iranEconomyNews by viewModel.iranEconomyNews.collectAsStateWithLifecycle()
    val cryptoNews by viewModel.cryptoNews.collectAsStateWithLifecycle()
    val techNews by viewModel.techNews.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (cryptoNews.isEmpty() && iranEconomyNews.isEmpty() && techNews.isEmpty()) {
            viewModel.refreshNews()
        }
    }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            Column(modifier = Modifier.background(ObsidianSlate900).statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📰 پیشخوان اخبار", style = DaraTypography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate50)
                    IconButton(onClick = { viewModel.refreshNews() }, enabled = !isRefreshing) {
                        if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = IndigoElectric)
                        else Icon(Icons.Default.Refresh, null, tint = IndigoElectric)
                    }
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = IndigoElectric,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = IndigoElectric,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    style = DaraTypography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Slate50 else Slate400
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val newsList = when (selectedTab) {
            0 -> iranEconomyNews
            1 -> cryptoNews
            else -> techNews
        }

        if (newsList.isEmpty() && isRefreshing) {
            DaraNewsRadarEmptyState()
        } else if (newsList.isEmpty()) {
            DaraNewsRadarEmptyState() // Also show radar if truly empty but not refreshing
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(newsList) { news ->
                    NewsCard(news, onClick = { onNewsClick(news) })
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun NewsCard(news: NewsEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSlate800),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f))
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
                    Text(news.source, style = DaraTypography.labelSmall, color = Slate400)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(PersianDateUtils.formatRelativeTime(news.publishedAt), style = DaraTypography.labelSmall, color = Slate400)
                }
                Text(
                    text = news.title,
                    style = DaraTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (news.aiSummary != null) {
                    Surface(
                        color = IndigoElectric.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, IndigoElectric.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "خلاصه هوشمند: ${news.aiSummary}",
                            modifier = Modifier.padding(10.dp),
                            style = DaraTypography.labelSmall,
                            color = Slate400,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SentimentBadge(news.sentiment)
                    news.relatedAssets?.split(",")?.take(2)?.forEach { asset ->
                        AssistChip(
                            onClick = {},
                            label = { Text(asset, style = DaraTypography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = Slate400, containerColor = ObsidianSlate700),
                            border = null,
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImportanceBadge(importance: String) {
    val result = when (importance) {
        "HIGH" -> RoseCoral to "فوری"
        "LOW" -> EmeraldCore to "عادی"
        else -> RefinedAmberGold to "مهم"
    }
    val color = result.first
    val text = result.second
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
        Text(text, color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = DaraTypography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SentimentBadge(sentiment: String) {
    val result = when (sentiment) {
        "POSITIVE" -> EmeraldCore to Icons.AutoMirrored.Filled.TrendingUp
        "NEGATIVE" -> RoseCoral to Icons.AutoMirrored.Filled.TrendingDown
        else -> Slate400 to Icons.AutoMirrored.Filled.TrendingFlat
    }
    val color = result.first
    val icon = result.second
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
            style = DaraTypography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
