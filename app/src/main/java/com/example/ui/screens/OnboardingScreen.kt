package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianSlate900)
    ) {
        // Background Glows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(IndigoElectric.copy(alpha = 0.08f), Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Monogram
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSlate600
                ) {
                    Icon(
                        imageVector = Icons.Default.Token,
                        contentDescription = null,
                        tint = IndigoElectric,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column {
                    Text("دارا", style = DaraTypography.titleLarge, color = Slate50, fontWeight = FontWeight.Bold)
                    Text("مدیریت ثروت هوشمند", style = DaraTypography.labelSmall, color = Slate400)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Hero Image
            Box(contentAlignment = Alignment.Center) {
                // Pulsing Halo
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(IndigoElectric.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAyWFu-oNyI0GI8j_h7y1wnJvLw8hnr6atEjR2iOqMyON4xq1J3D_sPSYjmJkssLDH_0T0Qxg1T-ZMVcdXx8K7FfFQkMIbp9tP4DVEPGQFhRK3IzXnB84VDXspVhDvL7PN-Axo8rqP7sQUsT1G2ZQu9wmhRTV-rrUMn706ek9BZhTPTQ6nkdizACyMSKods1TlivNWDC_yxGfuv8-GFyEzIEadl5R7oVsN2A8O4vR4qKZmt4_-F_Yn5",
                    contentDescription = "Dara Coin",
                    modifier = Modifier.size(240.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tagline
            Surface(
                shape = RoundedCornerShape(percent = 100),
                color = ObsidianSlate600,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Stars, null, tint = IndigoElectric, modifier = Modifier.size(16.dp))
                    Text("نسل نوین مدیریت ثروت هوشمند", style = DaraTypography.labelMedium, color = IndigoElectric, fontWeight = FontWeight.Bold)
                }
            }

            // Main Title with Gradient Text
            val annotatedTitle = buildAnnotatedString {
                append("همه‌ی دارایی‌ات، ")
                withStyle(style = SpanStyle(color = IndigoElectric)) {
                    append("یک‌جا")
                }
                append("، به زبان خودت")
            }
            Text(
                text = annotatedTitle,
                style = DaraTypography.displaySmall,
                color = Slate50,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "پایش و رشد هوشمند طلا، رمزارز، تتر، سهام و ملک بر پایه تحلیل‌های بی‌درنگ و سناریوهای سودآوری اختصاصی.",
                style = DaraTypography.bodyMedium,
                color = Slate400,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Feature Pills
            FeaturePill(
                icon = Icons.Default.AccountBalanceWallet,
                title = "پورتفوی یکپارچه و چندارزی",
                subtitle = "محاسبه خودکار ارزش ریالی، دلاری و طلا",
                iconColor = IndigoElectric
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeaturePill(
                icon = Icons.Default.Insights,
                title = "تحلیل روند بازار و سناریونویسی",
                subtitle = "هشدار نوسانات تورمی و بازتنظیم پرتفوی",
                iconColor = RefinedAmberGold
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeaturePill(
                icon = Icons.Default.VerifiedUser,
                title = "امنیت بانکی رمزنگاری‌شده AES-256",
                subtitle = "کنترل تمام‌عیار کاربر روی تمامی داده‌ها",
                iconColor = EmeraldCore
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary Button
            Button(
                onClick = onFinishOnboarding,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoElectric
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ArrowForward, null)
                    Text("شروع کنید", style = DaraTypography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* Login */ }) {
                Text(
                    text = buildAnnotatedString {
                        append("قبلاً حساب ساخته‌اید؟ ")
                        withStyle(style = SpanStyle(color = IndigoElectric, fontWeight = FontWeight.Bold)) {
                            append("ورود به دارا")
                        }
                    },
                    style = DaraTypography.bodyMedium,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
private fun FeaturePill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSlate800.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = ObsidianSlate700
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(10.dp)
            )
        }
        Column {
            Text(title, style = DaraTypography.titleSmall, color = Slate50, fontWeight = FontWeight.Bold)
            Text(subtitle, style = DaraTypography.labelSmall, color = Slate400)
        }
    }
}
