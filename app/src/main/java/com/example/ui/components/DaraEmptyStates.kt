package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun DaraNewsRadarEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
            // Radar Animation Rings
            RadarRipple()
            
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida/AEtjO1WZZRKrP92OMWD79y1biTthmDfkvPVyajDQFI9_t6GWBzKIzizeYUvSWDqelYoHagjgJiYIxhtJShbyMXctIENYdHNRqZqJXbGCGV2O9PBWRBgMXTaYQO8TERNwGkA2OGcUQlrOypzvyGNDSuNhqF9N7KWeLNtfdJRG-wFaFtYRBtVCvw1tEen1lcU7DzvauSOaRr4NnNWJjVJvbGGVR-5lN4LfGnDsQikNJkaYeXNSXPf-CQLVA_S-1p4",
                contentDescription = "Radar",
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "در حال پایش و جستجوی عمیق اخبار مالی...",
            style = DaraTypography.headlineSmall,
            color = Slate50,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "رادار هوش مصنوعی دارا به‌طور مداوم خبرگزاری‌ها و صرافی‌های جهانی را رصد می‌کند.",
            style = DaraTypography.bodySmall,
            color = Slate400,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            color = ObsidianSlate800,
            shape = RoundedCornerShape(percent = 100)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(EmeraldCore, CircleShape))
                Text("سیستم پایش زنده فعال است", style = DaraTypography.labelSmall, color = Slate50)
            }
        }
    }
}

@Composable
fun RadarRipple() {
    val transition = rememberInfiniteTransition(label = "radar")
    val scale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha"
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .background(IndigoElectric.copy(alpha = 0.2f), CircleShape)
    )
}
