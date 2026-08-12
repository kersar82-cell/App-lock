package com.focuslock.app

import android.content.Context
import java.security.MessageDigest

/**
 * সব সেটিংস (PIN হ্যাশ, ব্লকলিস্ট, টাইমার এন্ড টাইম) এখানে SharedPreferences এ সেভ থাকে।
 * PIN কখনো প্লেইন-টেক্সটে সেভ হয় না, SHA-256 হ্যাশ আকারে সেভ হয়।
 */
object PrefsManager {
    private const val PREFS = "focuslock_prefs"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_BLOCKED_APPS = "blocked_apps"
    private const val KEY_TIMER_END = "timer_end_millis"
    private const val KEY_BLOCKING_ACTIVE = "blocking_active"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isPinSet(context: Context): Boolean =
        prefs(context).contains(KEY_PIN_HASH)

    fun setPin(context: Context, pin: String) {
        prefs(context).edit().putString(KEY_PIN_HASH, sha256(pin)).apply()
    }

    fun checkPin(context: Context, pin: String): Boolean {
        val saved = prefs(context).getString(KEY_PIN_HASH, null) ?: return false
        return saved == sha256(pin)
    }

    fun getBlockedApps(context: Context): MutableSet<String> =
        HashSet(prefs(context).getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet())

    fun setBlockedApps(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_BLOCKED_APPS, packages).apply()
    }

    fun setTimerEnd(context: Context, endMillis: Long) {
        prefs(context).edit()
            .putLong(KEY_TIMER_END, endMillis)
            .putBoolean(KEY_BLOCKING_ACTIVE, true)
            .apply()
    }

    fun getTimerEnd(context: Context): Long =
        prefs(context).getLong(KEY_TIMER_END, 0L)

    fun isBlockingActive(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BLOCKING_ACTIVE, false)

    fun stopBlocking(context: Context) {
        prefs(context).edit().putBoolean(KEY_BLOCKING_ACTIVE, false).apply()
    }
}
