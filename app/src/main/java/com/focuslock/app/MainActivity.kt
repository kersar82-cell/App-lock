package com.focuslock.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var etMinutes: TextInputEditText
    private lateinit var btnStartTimer: MaterialButton
    private lateinit var btnStopTimer: MaterialButton
    private lateinit var btnBatterySettings: MaterialButton
    private lateinit var btnExam: MaterialButton
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etMinutes = findViewById(R.id.etMinutes)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnStopTimer = findViewById(R.id.btnStopTimer)
        btnBatterySettings = findViewById(R.id.btnBatterySettings)
        btnExam = findViewById(R.id.btnExam)
        tvStatus = findViewById(R.id.tvStatus)

        btnStartTimer.setOnClickListener {
            val minsText = etMinutes.text.toString()
            if (minsText.isNotEmpty()) {
                val mins = minsText.toIntOrNull() ?: 0
                tvStatus.text = "$mins মিনিটের টাইমার চলছে..."

                val serviceIntent = Intent(this, TimerService::class.java).apply {
                    action = TimerService.ACTION_START_TIMER
                    putExtra(TimerService.EXTRA_MINUTES, mins)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                Toast.makeText(this, "টাইমার শুরু হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "মিনিট লিখুন", Toast.LENGTH_SHORT).show()
            }
        }

        btnStopTimer.setOnClickListener {
            tvStatus.text = "টাইমার বন্ধ হয়েছে"
            val serviceIntent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_STOP_TIMER
            }
            startService(serviceIntent)
            Toast.makeText(this, "টাইমার বন্ধ", Toast.LENGTH_SHORT).show()
        }

        btnBatterySettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        btnExam.setOnClickListener {
            Toast.makeText(this, "শব্দার্থ এক্সাম ফিচারটি শীঘ্রই আসছে!", Toast.LENGTH_SHORT).show()
        }
    }
}
