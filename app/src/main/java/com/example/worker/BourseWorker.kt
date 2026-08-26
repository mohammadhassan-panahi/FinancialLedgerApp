package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.IpoEntity

class BourseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val bourseDao = database.bourseDao()

        return try {
            // Simulated fetch of new IPOs - In a real app, this hits a Bourse/Codal API
            val currentIpos = bourseDao.getActiveIpos()
            
            // Logic to detect NEW IPOs
            // For now, we simulate finding a new IPO
            if (currentIpos.isEmpty()) {
                val demoIpo = IpoEntity(
                    symbol = "پترو",
                    companyName = "پتروشیمی خلیج فارس",
                    ipoDate = "۱۴۰۳/۰۶/۱۵",
                    maxShares = 1000,
                    maxPriceRial = 15000.0,
                    minPriceRial = 14000.0,
                    requiredLiquidityRial = 15000000.0,
                    status = "به زودی"
                )
                bourseDao.insertIpos(listOf(demoIpo))
                sendNotification("عرضه اولیه جدید!", "نماد «${demoIpo.symbol}» به زودی عرضه می‌شود.")
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "bourse_alerts_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "اطلاعیه‌های بورس",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان عرضه‌های اولیه و پیام‌های مهم کدال"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(3001, notification)
    }
}
