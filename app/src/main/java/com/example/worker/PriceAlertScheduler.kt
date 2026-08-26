package com.example.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object PriceAlertScheduler {
    private const val WORK_NAME = "price_alert_check"
    private const val BOURSE_WORK_NAME = "bourse_monitor"

    /** Schedules a periodic background check for prices and bourse alerts. */
    fun schedule(context: Context) {
        val wm = WorkManager.getInstance(context)

        // Price Alert Check (30 min)
        val alertRequest = PeriodicWorkRequestBuilder<PriceAlertWorker>(30, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, alertRequest)

        // Bourse/IPO Monitor (3 hours)
        val bourseRequest = PeriodicWorkRequestBuilder<BourseWorker>(3, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork(BOURSE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, bourseRequest)
    }
}
