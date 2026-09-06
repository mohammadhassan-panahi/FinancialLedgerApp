package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.viewmodel.AiAnalysisViewModel
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    viewModel: AiAnalysisViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            AiChatHeader(
                onBack = onBack,
                onClearChat = { viewModel.clearChat() }
            )
        },
        bottomBar = {
            ChatInputDock(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                isLoading = isLoading
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Glow
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(IndigoElectric.copy(alpha = 0.05f), Color.Transparent)
                    )
                )
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DaraPersonaGreeting()
                }

                item {
                    SuggestedPromptsRow(onPromptClick = { viewModel.sendMessage(it) })
                }

                items(messages) { message ->
                    ChatBubble(message)
                }
                
                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun AiChatHeader(onBack: () -> Unit, onClearChat: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = ObsidianSlate900.copy(alpha = 0.8f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.height(72.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate50)
                }
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida/AEtjO1WKb-JdsP2NuvqL2iG_BxlPdgxrOhrOUd38Uq79YflzYtfw0btA5u1Leayr4ywNjITQ0m4tqK6H_6JcBKS_ctY9J9Mexqqim6vyQb1ktoMpFvZ9IB7ID0fbHW8B-gTkwM7ffip97krcMQFlUfqJAw6PTpe9RqefbKcdV4VcCKyRc24z_m8a81BghgI9zL__-G4zB8gsE0CVFM8OZdwyjQgSv-wLplJHAF-SgdtyiYDfAMeCs8jIQ9G3iDc",
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text("Ai Wealth Chat", style = DaraTypography.titleMedium, color = Slate50, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(EmeraldCore, CircleShape))
                        Text("آنلاین - مدل دارا v4.2", style = DaraTypography.labelSmall, color = EmeraldCore)
                    }
                }
            }
            IconButton(onClick = onClearChat) {
                Icon(Icons.Default.DeleteSweep, null, tint = Slate400)
            }
        }
    }
}

@Composable
fun DaraPersonaGreeting() {
    Surface(
        color = ObsidianSlate800.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(IndigoElectric.copy(alpha = 0.2f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SmartToy, null, tint = IndigoElectric, modifier = Modifier.size(16.dp))
                }
                Text("دستیار هوشمند دارا", style = DaraTypography.labelMedium, color = IndigoElectric, fontWeight = FontWeight.Bold)
            }
            Text(
                "سلام علیرضا عزیز؛ من دارا هستم؛ مشاور مالی اختصاصی شما. امروز چطور می‌تونم به رشد دارایی‌هات کمک کنم؟",
                style = DaraTypography.bodyLarge,
                color = Slate50,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun SuggestedPromptsRow(onPromptClick: (String) -> Unit) {
    val prompts = listOf(
        "زمان مناسب خرید طلا؟",
        "تحلیل پورتفوی من",
        "ریسک دلار vs کریپتو"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(prompts) { prompt ->
            AssistChip(
                onClick = { onPromptClick(prompt) },
                label = { Text(prompt, style = DaraTypography.labelSmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = ObsidianSlate800,
                    labelColor = Slate400
                ),
                shape = CircleShape,
                border = null
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) IndigoElectric else ObsidianSlate800
    val textColor = if (message.isUser) Color.White else Slate50
    val shape = if (message.isUser) 
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    else 
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = shape,
            tonalElevation = 2.dp
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = DaraTypography.bodyMedium,
                color = textColor,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun ChatInputDock(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        color = ObsidianSlate800.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { /* Attach */ }) {
                Icon(Icons.Default.Add, null, tint = Slate400)
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("از دارا بپرس...", style = DaraTypography.bodyMedium, color = Slate600) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Slate50
                ),
                textStyle = DaraTypography.bodyMedium
            )
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (text.isNotBlank()) IndigoElectric else Color.Transparent,
                    contentColor = if (text.isNotBlank()) Color.White else Slate600
                )
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = IndigoElectric, strokeWidth = 2.dp)
                else Icon(Icons.AutoMirrored.Filled.Send, null)
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) {
            Box(modifier = Modifier.size(6.dp).background(IndigoElectric.copy(alpha = 0.4f), CircleShape))
        }
    }
}
