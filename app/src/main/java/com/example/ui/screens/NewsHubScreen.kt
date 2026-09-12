package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.NewsViewModel
import com.example.util.PersianDateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsHubScreen(
    viewModel: NewsViewModel,
    onBack: () -> Unit,
    onNewsClick: (NewsEntity) -> Unit
) {
    val cryptoNews by viewModel.cryptoNews.collectAsStateWithLifecycle()
    val economyNews by viewModel.iranEconomyNews.collectAsStateWithLifecycle()
    val techNews by viewModel.techNews.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("کریپتو", "اقتصاد ایران", "تکنولوژی")

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("پیشخوان خبر دارا", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshNews() }, enabled = !isRefreshing) {
                        Icon(Icons.Default.Refresh, null, tint = IndigoElectric)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = IndigoElectric,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, modifier = Modifier.padding(vertical = 12.dp)) }
                    )
                }
            }

            val displayNews = when (selectedTab) {
                0 -> cryptoNews
                1 -> economyNews
                2 -> techNews
                else -> emptyList()
            }

            if (isRefreshing && displayNews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IndigoElectric)
                }
            } else if (displayNews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("خبری یافت نشد. بروزرسانی کنید.", color = Slate600)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayNews) { news ->
                        NewsCard(news = news, onClick = { onNewsClick(news) })
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(news: NewsEntity, onClick: () -> Unit) {
    DaraGlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            news.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(color = IndigoElectric.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            news.source,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = DaraTypography.labelSmall,
                            color = IndigoElectric,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        PersianDateUtils.formatRelativeTime(news.publishedAt),
                        style = DaraTypography.labelSmall,
                        color = Slate600
                    )
                }

                Text(
                    news.title,
                    style = DaraTypography.titleSmall,
                    color = Slate50,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                news.description?.let {
                    Text(
                        it,
                        style = DaraTypography.bodySmall,
                        color = Slate400,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                
                if (news.aiSummary != null) {
                    Surface(color = EmeraldCore.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, null, tint = EmeraldCore, modifier = Modifier.size(14.dp))
                            Text("دارای خلاصه هوشمند", style = DaraTypography.labelSmall, color = EmeraldCore)
                        }
                    }
                }
            }
        }
    }
}
