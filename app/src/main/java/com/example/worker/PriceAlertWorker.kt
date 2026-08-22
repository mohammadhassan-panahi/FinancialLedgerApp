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
import com.example.data.repository.PortfolioRepository
import com.example.util.formatRial

/**
 * Runs periodically (see PriceAlertScheduler), refreshes gold/dollar + watchlist rates,
 * then checks all active PriceAlertEntity rows and fires a notification for each hit.
 */
class PriceAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val apiKey = com.example.BuildConfig.BRSAPI_KEY
        val repository = PortfolioRepository(
            purchaseDao = database.assetPurchaseDao(),
            saleDao = database.assetSaleDao(),
            marketDao = database.marketDao(),
            stockDao = database.stockDao(),
            alertDao = database.priceAlertDao(),
            apiKey = apiKey
        )

        return try {
            val watchlistSymbols = database.stockDao().getWatchlistOnce().map { it.symbol }
            repository.refreshGoldAndDollar()
            if (watchlistSymbols.isNotEmpty()) repository.refreshWatchlist(watchlistSymbols)

            val rates = database.marketDao().getAllMarketRatesOnce()
            val stocks = database.stockDao().getAllSymbolsOnce()
            val triggered = repository.checkAlerts(rates, stocks)

            triggered.forEach { alert ->
                sendNotification(
                    title = "هشدار قیمت: ${alert.assetName}",
                    message = "قیمت به ${formatRial(alert.targetPriceRial)} رسید."
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "price_alerts_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "هشدار قیمت",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان رسیدن قیمت طلا، دلار یا سهام به هدف تعیین‌شده"
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

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
