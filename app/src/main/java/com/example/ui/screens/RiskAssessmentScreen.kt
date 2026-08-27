package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.RiskAssessmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentScreen(
    viewModel: RiskAssessmentViewModel,
    onFinished: () -> Unit
) {
    val profile by viewModel.riskProfile.collectAsStateWithLifecycle()
    var currentStep by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }

    val questions = listOf(
        "هدف اصلی شما از سرمایه‌گذاری چیست؟" to listOf("حفظ ارزش پول (۱۰ امتیاز)", "سود میان‌مدت (۵۰ امتیاز)", "ثروت‌آفرینی سریع (۹۰ امتیاز)"),
        "اگر بازار ۲۰٪ سقوط کند، چه می‌کنید؟" to listOf("می‌فروشم و خارج می‌شوم (۱۰ امتیاز)", "صبر می‌کنم (۵۰ امتیاز)", "بیشتر می‌خرم (۹۰ امتیاز)"),
        "چه مدت می‌توانید سرمایه خود را بلوکه کنید؟" to listOf("کمتر از یک سال (۱۰ امتیاز)", "۱ تا ۳ سال (۵۰ امتیاز)", "بیش از ۳ سال (۹۰ امتیاز)")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("آزمون ریسک‌پذیری") })
        },
        containerColor = DarkSlateSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profile != null && currentStep == 0) {
                Text("شخصیت مالی شما قبلاً ثبت شده است:", color = TextSecondary)
                Text(profile!!.personalityType, style = MaterialTheme.typography.headlineSmall, color = CredifyIndigo, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { currentStep = 1; score = 0 }) {
                    Text("آزمون مجدد")
                }
            } else if (currentStep <= questions.size && currentStep > 0) {
                val q = questions[currentStep - 1]
                Text("سوال $currentStep از ${questions.size}", color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(q.first, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                q.second.forEachIndexed { index, option ->
                    val optionScore = when(index) { 0 -> 10; 1 -> 50; else -> 90 }
                    Button(
                        onClick = { 
                            score += optionScore
                            if (currentStep < questions.size) {
                                currentStep++
                            } else {
                                viewModel.submitAssessment(score / questions.size)
                                onFinished()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorderLight)
                    ) {
                        Text(option, color = TextPrimary)
                    }
                }
            } else {
                Text("برای شروع آزمون دکمه زیر را بزنید", color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { currentStep = 1 }) {
                    Text("شروع آزمون")
                }
            }
        }
    }
}
