package com.focuslock.app

import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * টাইমার চলাকালীন একটা persistent notification দেখায় (কতক্ষণ বাকি)।
 * টাইমার শেষ হলে DevicePolicyManager.lockNow() কল করে ফোন সরাসরি লক করে দেয়,
 * এবং ব্লকিং state বন্ধ করে দেয় (চাইলে আপনি এটা "শেষেও ব্লক থাকবে" করে বদলাতে পারেন)।
 */
class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "focuslock_timer_channel"
        const val NOTIF_ID = 1001
        const val EXTRA_DURATION_MILLIS = "duration_millis"
        const val ACTION_STOP = "com.focuslock.app.ACTION_STOP_TIMER"
    }

    private var countDownTimer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTimerAndService()
            return START_NOT_STICKY
        }

        val duration = intent?.getLongExtra(EXTRA_DURATION_MILLIS, 0L) ?: 0L
        if (duration <= 0L) {
            stopSelf()
            return START_NOT_STICKY
        }

        PrefsManager.setTimerEnd(this, System.currentTimeMillis() + duration)
        startForeground(NOTIF_ID, buildNotification(duration))
        startCountdown(duration)
        return START_STICKY
    }

    private fun startCountdown(duration: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIF_ID, buildNotification(millisUntilFinished))
            }

            override fun onFinish() {
                lockPhoneNow()
                // ব্লকিং চালিয়ে যেতে চাইলে নিচের লাইনটা কমেন্ট করে রাখুন
                PrefsManager.stopBlocking(this@TimerService)
                stopTimerAndService()
            }
        }.start()
    }

    private fun lockPhoneNow() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, LockAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        }
    }

    private fun stopTimerAndService() {
        countDownTimer?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(millisRemaining: Long): Notification {
        createChannelIfNeeded()
        val minutes = (millisRemaining / 1000) / 60
        val seconds = (millisRemaining / 1000) % 60

        // নোটিফিকেশন থেকে "বন্ধ করুন" চাপলে সরাসরি টাইমার বন্ধ হয় না —
        // আগে PinActivity খুলে PIN যাচাই করা হয়, তারপর তা টাইমার বন্ধ করে।
        val stopIntent = Intent(this, PinActivity::class.java).apply {
            action = PinActivity.ACTION_STOP_TIMER
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val stopPendingIntent = PendingIntent.getActivity(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ফোকাস টাইমার চলছে")
            .setContentText(String.format("বাকি সময়: %02d:%02d", minutes, seconds))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .addAction(0, "বন্ধ করুন (PIN লাগবে)", stopPendingIntent)
            .build()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Focus Timer", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
