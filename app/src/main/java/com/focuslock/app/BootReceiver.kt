package com.focuslock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ফোন রিস্টার্ট হলে যদি টাইমার আগে থেকে চলমান থাকে (সময় এখনও বাকি),
 * তাহলে বাকি সময়ের জন্য আবার TimerService চালু করে দেয়।
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!PrefsManager.isBlockingActive(context)) return

        val remaining = PrefsManager.getTimerEnd(context) - System.currentTimeMillis()
        if (remaining > 1000) {
            val serviceIntent = Intent(context, TimerService::class.java)
                .putExtra(TimerService.EXTRA_DURATION_MILLIS, remaining)
            context.startForegroundService(serviceIntent)
        } else {
            PrefsManager.stopBlocking(context)
        }
    }
}
