package com.example.marketlens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PriceAlertWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "price_alerts"
        const val WORK_NAME = "price_alert_check"
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(appContext)
        val alerts = db.alertDao().getAllAlerts()
        if (alerts.isEmpty()) return Result.success()

        // Reuse cache if fresh (< 5 min), otherwise fetch
        val currency = "usd"
        val coins = try {
            val cached = AppCache.coinCache[currency]
            if (cached != null && (System.currentTimeMillis() - cached.second) < 300_000L) {
                cached.first
            } else {
                val fetched = RetrofitClient.api.getCoins(currency = currency)
                AppCache.coinCache[currency] = Pair(fetched, System.currentTimeMillis())
                fetched
            }
        } catch (e: Exception) {
            return Result.retry()
        }

        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(notificationManager)

        for (alert in alerts) {
            val coin = coins.find { it.id == alert.coinId } ?: continue
            val triggered = if (alert.isAbove) coin.currentPrice >= alert.targetPrice
                            else coin.currentPrice <= alert.targetPrice
            if (triggered) {
                val direction = if (alert.isAbove) "above" else "below"
                val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("${alert.coinName} Price Alert")
                    .setContentText(
                        "${alert.coinName} is now $direction \$${String.format("%,.2f", alert.targetPrice)}"
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                notificationManager.notify(alert.id, notification)
                db.alertDao().deleteAlert(alert)
            }
        }
        return Result.success()
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Price Alerts", NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifies when a coin hits your target price"
            manager.createNotificationChannel(channel)
        }
    }
}
