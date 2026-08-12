package com.focuslock.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.focuslock.app.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.focuslock.app.ACTION_STOP_TIMER"
        const val EXTRA_MINUTES = "extra_minutes"
        const val EXTRA_DURATION_MILLIS = "extra_duration_millis"
        const val CHANNEL_ID = "FocusLockTimerChannel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
                val durationMillis = intent.getLongExtra(EXTRA_DURATION_MILLIS, minutes.toLong() * 60 * 1000)
                val displayMins = if (minutes > 0) minutes else (durationMillis / 60000).toInt()
                startForegroundServiceWithNotification(displayMins)
            }
            ACTION_STOP_TIMER -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification(minutes: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusLock Timer")
            .setContentText("টাইমার চলছে ($minutes মিনিট)")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
