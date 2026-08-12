package com.focuslock.app

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin এক্টিভ থাকলে দুইটা জিনিস হয়:
 * ১) আমরা DevicePolicyManager.lockNow() কল করে ফোন সরাসরি লক করতে পারি।
 * ২) সিস্টেম নিজে থেকেই "App Info" স্ক্রিনে Uninstall বাটন ব্লক করে রাখে,
 *    যতক্ষণ না ইউজার আগে Settings > Security > Device Admin থেকে এটাকে
 *    Deactivate করছে। এটা সরাসরি ইউনিনস্টল আটকায় না, কিন্তু একটা
 *    বাড়তি ধাপ তৈরি করে।
 */
class LockAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "FocusLock অ্যাডমিন চালু হয়েছে", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Device Admin বন্ধ করলে টাইমার-শেষে অটো-লক ফিচারটি আর কাজ করবে না। আপনি কি নিশ্চিত?"
    }
}
