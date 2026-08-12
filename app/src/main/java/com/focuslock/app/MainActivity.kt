package com.focuslock.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.focuslock.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var deviceAdminComponent: ComponentName
    private var selectedApps = mutableSetOf<String>()

    private val pinResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // PIN ঠিকঠাক সেট/ভেরিফাই হয়েছে
            refreshStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceAdminComponent = ComponentName(this, LockAdminReceiver::class.java)
        selectedApps = PrefsManager.getBlockedApps(this)

        setupAppList()

        binding.btnSetPin.setOnClickListener {
            startActivity(Intent(this, PinActivity::class.java))
        }

        binding.btnDeviceAdmin.setOnClickListener { requestDeviceAdmin() }
        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "তালিকায় 'FocusLock' খুঁজে চালু করুন", Toast.LENGTH_LONG).show()
        }
        binding.btnBatteryOptOut.setOnClickListener { requestBatteryOptOut() }

        binding.btnStartTimer.setOnClickListener { onStartTimerClicked() }

        binding.btnStopTimer.setOnClickListener {
            if (!PrefsManager.isPinSet(this)) {
                Toast.makeText(this, "আগে PIN সেট করুন", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, PinActivity::class.java).apply {
                action = PinActivity.ACTION_STOP_TIMER
            }
            pinResultLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun setupAppList() {
        val pm = packageManager
        val launchableApps = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
        val apps = launchableApps
            .filter { it.activityInfo.packageName != packageName }
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = AppListAdapter(apps, selectedApps)
    }

    private fun onStartTimerClicked() {
        if (!PrefsManager.isPinSet(this)) {
            Toast.makeText(this, "প্রথমে একটা PIN সেট করুন, যাতে পরে আপনি ছাড়া কেউ টাইমার বন্ধ করতে না পারে", Toast.LENGTH_LONG).show()
            return
        }
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (!dpm.isAdminActive(deviceAdminComponent)) {
            Toast.makeText(this, "টাইমার শেষে ফোন লক করতে হলে আগে 'Device Admin' চালু করুন", Toast.LENGTH_LONG).show()
            return
        }

        val minutesText = binding.etMinutes.text.toString()
        val minutes = minutesText.toLongOrNull()
        if (minutes == null || minutes <= 0) {
            Toast.makeText(this, "কত মিনিট পর লক হবে, সেটা লিখুন", Toast.LENGTH_SHORT).show()
            return
        }

        // সিলেক্ট করা ব্লকলিস্ট সেভ করুন
        PrefsManager.setBlockedApps(this, selectedApps)

        val durationMillis = minutes * 60 * 1000
        val serviceIntent = Intent(this, TimerService::class.java)
            .putExtra(TimerService.EXTRA_DURATION_MILLIS, durationMillis)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "টাইমার শুরু হয়েছে — $minutes মিনিট পর ফোন লক হয়ে যাবে", Toast.LENGTH_LONG).show()
        refreshStatus()
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent)
            putExtra(
                android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "টাইমার শেষে ফোন লক করতে এবং অ্যাপটি সহজে আনইনস্টল হওয়া থেকে রক্ষা করতে এই পারমিশন দরকার।"
            )
        }
        startActivity(intent)
    }

    private fun requestBatteryOptOut() {
        val packageName = packageName
        val pm = getSystemService(android.os.PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "ইতিমধ্যে ব্যাটারি অপ্টিমাইজেশনের বাইরে আছে", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "$packageName/${BlockAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    private fun refreshStatus() {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminOn = dpm.isAdminActive(deviceAdminComponent)
        val accessibilityOn = isAccessibilityServiceEnabled()
        val pinSet = PrefsManager.isPinSet(this)

        binding.tvStatus.text = buildString {
            append(if (pinSet) "✅ PIN সেট আছে\n" else "❌ PIN সেট করা নেই\n")
            append(if (adminOn) "✅ Device Admin চালু\n" else "❌ Device Admin বন্ধ\n")
            append(if (accessibilityOn) "✅ Accessibility চালু\n" else "❌ Accessibility বন্ধ\n")
            if (PrefsManager.isBlockingActive(this@MainActivity)) {
                val remainingMs = PrefsManager.getTimerEnd(this@MainActivity) - System.currentTimeMillis()
                val remainingMin = (remainingMs / 1000 / 60).coerceAtLeast(0)
                append("⏱️ টাইমার চলছে — প্রায় $remainingMin মিনিট বাকি")
            } else {
                append("⏱️ টাইমার বন্ধ আছে")
            }
        }
    }
}
