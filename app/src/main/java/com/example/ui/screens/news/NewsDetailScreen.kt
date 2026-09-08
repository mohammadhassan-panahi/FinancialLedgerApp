package com.example.ui.screens.news

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.NewsEntity
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.util.PersianDateUtils

@Composable
fun NewsDetailScreen(
    news: NewsEntity,
    onBack: () -> Unit,
    onChatWithAi: () -> Unit
) {
    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            NewsDetailHeader(onBack = onBack)
        },
        floatingActionButton = {
            FloatingAiAssistantButton(onClick = onChatWithAi)
        }
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // News Image & Category info
            NewsHeroSection(news)

            // AI Snapshot Box
            DaraAiSnapshotBox(news)

            // News Content
            NewsContentBody(news)

            // AI Inquiry Card
            AiInquiryCard(onChatWithAi)
        }
    }
}

@Composable
fun NewsDetailHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate50)
                }
                Column {
                    Text("جزئیات خبر", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("تحلیل هوشمند دارا", style = DaraTypography.labelSmall, color = EmeraldCore)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.BookmarkBorder, null, tint = Slate400)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, null, tint = Slate400)
                }
            }
        }
    }
}

@Composable
fun NewsHeroSection(news: NewsEntity) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = RefinedAmberGold.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = news.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = DaraTypography.labelSmall,
                    color = RefinedAmberGold,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${PersianDateUtils.formatRelativeTime(news.publishedAt)} • مطالعه ۴ دقیقه",
                style = DaraTypography.labelSmall,
                color = Slate400
            )
        }

        Text(
            text = news.title,
            style = DaraTypography.headlineLarge,
            color = Slate50,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 36.sp
        )

        if (news.imageUrl != null) {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun DaraAiSnapshotBox(news: NewsEntity) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp).background(IndigoElectric.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("خلاصه هوشمند دارا", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
                        Text("Dara AI Snapshot", style = DaraTypography.labelSmall, color = IndigoElectric.copy(alpha = 0.7f))
                    }
                }
                Surface(color = ObsidianSlate600, shape = CircleShape) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, null, tint = RefinedAmberGold, modifier = Modifier.size(14.dp))
                        Text("آنی", style = DaraTypography.labelSmall, color = RefinedAmberGold)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AiBullet(text = news.aiSummary ?: "در انتظار تحلیل هوشمند...", color = EmeraldCore)
                val sentimentText = if (news.sentiment == "POSITIVE") "مثبت" else if (news.sentiment == "NEGATIVE") "منفی" else "خنثی"
                AiBullet(text = "سنتیمنت بازار: $sentimentText", color = RefinedAmberGold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val impact = if (news.importance == "HIGH") 0.9f else if (news.importance == "MEDIUM") 0.6f else 0.3f
                MetricMeter(label = "اثرگذاری بر بازار", value = impact, color = RefinedAmberGold, modifier = Modifier.weight(1f))
                MetricMeter(label = "سنتیمنت تحلیل", value = 0.75f, color = EmeraldCore, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AiBullet(text: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(6.dp).padding(top = 8.dp).clip(CircleShape).background(color))
        Text(text, style = DaraTypography.bodyMedium, color = Slate400, lineHeight = 22.sp)
    }
}

@Composable
private fun MetricMeter(label: String, value: Float, color: Color, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = DaraTypography.labelSmall, color = Slate400)
            Text("${(value * 100).toInt()}%", style = DaraTypography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = color,
            trackColor = ObsidianSlate600
        )
    }
}

@Composable
fun NewsContentBody(news: NewsEntity) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            text = news.description ?: "متن خبر در دسترس نیست.",
            style = DaraTypography.bodyLarge,
            color = Slate50.copy(alpha = 0.9f),
            lineHeight = 28.sp,
            textAlign = TextAlign.Justify
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianSlate800)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(EmeraldCore, CircleShape))
                Column {
                    Text(
                        "منبع خبر: ${news.source}",
                        style = DaraTypography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }
    }
}

@Composable
fun AiInquiryCard(onChat: () -> Unit) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).background(IndigoElectric, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("بینش سریع هوشمند دارا", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Text("برای تحلیل دقیق‌تر اثر این خبر بر پورتفوی شخصی‌تان، سؤالتان را بپرسید:", style = DaraTypography.bodyMedium, color = Slate400)
            
            Button(
                onClick = onChat,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric)
            ) {
                Text("گفت‌وگو با دستیار درباره این خبر", style = DaraTypography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FloatingAiAssistantButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(48.dp).padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSlate700,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, RefinedAmberGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(24.dp).background(
                    Brush.linearGradient(listOf(IndigoElectric, RefinedAmberGold)),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Text("دستیار هوشمند", style = DaraTypography.labelMedium, color = Slate50, fontWeight = FontWeight.Bold)
        }
    }
}
