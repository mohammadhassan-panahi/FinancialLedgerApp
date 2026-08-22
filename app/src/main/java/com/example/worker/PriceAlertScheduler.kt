package com.example.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object PriceAlertScheduler {
    private const val WORK_NAME = "price_alert_check"

    /** Schedules a periodic (every 30 min — WorkManager's practical minimum is 15) background price check. */
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
