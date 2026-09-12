package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.NewsEntity
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.util.PersianDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: NewsEntity,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("جزئیات خبر", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                        context.startActivity(intent)
                    }) { Icon(Icons.Default.OpenInBrowser, null, tint = IndigoElectric) }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${news.title}\n\n${news.url}")
                        }
                        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری خبر"))
                    }) { Icon(Icons.Default.Share, null, tint = Slate400) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            news.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = IndigoElectric.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            news.source,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = DaraTypography.labelMedium,
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
                    style = DaraTypography.headlineSmall,
                    color = Slate50,
                    fontWeight = FontWeight.Black,
                    lineHeight = 32.sp
                )

                HorizontalDivider(color = ObsidianSlate700)

                if (news.aiSummary != null) {
                    AiSummaryCard(summary = news.aiSummary)
                }

                Text(
                    news.description ?: "",
                    style = DaraTypography.bodyLarge,
                    color = Slate400,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianSlate700)
                ) {
                    Text("مشاهده متن کامل خبر در منبع", style = DaraTypography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun AiSummaryCard(summary: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1B4B),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = IndigoElectric, modifier = Modifier.size(20.dp))
                Text("خلاصه هوشمند دارا (AI)", style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
            }
            Text(
                summary,
                style = DaraTypography.bodyMedium,
                color = Slate50.copy(alpha = 0.9f),
                lineHeight = 24.sp
            )
        }
    }
}
