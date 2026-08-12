package com.focuslock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("FocusLockPrefs", Context.MODE_PRIVATE)
            val isTimerRunning = prefs.getBoolean("is_timer_running", false)
            val remainingTime = prefs.getLong("remaining_time_ms", 0L)

            if (isTimerRunning && remainingTime > 0) {
                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_START_TIMER
                    putExtra(TimerService.EXTRA_DURATION_MILLIS, remainingTime)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
