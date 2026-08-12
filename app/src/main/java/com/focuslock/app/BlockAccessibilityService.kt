package com.focuslock.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * এই সার্ভিসটা প্রতিবার স্ক্রিনে অ্যাপ পাল্টালে (foreground app change) নোটিফাই পায়।
 * যদি টাইমার/ব্লকিং চালু থাকে এবং নতুন অ্যাপটা ব্লকলিস্টে থাকে,
 * সাথে সাথে ইউজারকে হোম স্ক্রিনে পাঠিয়ে দেওয়া হয় — অ্যাপটা "খোলা" অবস্থায় থাকতে পারে না।
 * (এটাই আসল "Force Stop" এর কাজ করে, যেহেতু সরাসরি force-stop API সাধারণ অ্যাপের জন্য নিষিদ্ধ।)
 */
class BlockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        if (!PrefsManager.isBlockingActive(this)) return
        if (packageName == this.packageName) return // নিজের অ্যাপকে বাদ দিন

        val blocked = PrefsManager.getBlockedApps(this)
        if (blocked.contains(packageName)) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(homeIntent)
        }
    }

    override fun onInterrupt() {}
}
