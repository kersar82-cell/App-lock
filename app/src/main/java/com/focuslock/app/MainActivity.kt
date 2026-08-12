package com.focuslock.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnDeviceAdmin: MaterialButton
    private lateinit var btnAccessibility: MaterialButton
    private lateinit var btnBatteryOptOut: MaterialButton
    private lateinit var btnSetPin: MaterialButton
    private lateinit var etMinutes: TextInputEditText
    private lateinit var btnSelectApps: MaterialButton
    private lateinit var rvApps: RecyclerView
    private lateinit var btnStartTimer: MaterialButton
    private lateinit var btnStopTimer: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnDeviceAdmin = findViewById(R.id.btnDeviceAdmin)
        btnAccessibility = findViewById(R.id.btnAccessibility)
        btnBatteryOptOut = findViewById(R.id.btnBatteryOptOut)
        btnSetPin = findViewById(R.id.btnSetPin)
        etMinutes = findViewById(R.id.etMinutes)
        btnSelectApps = findViewById(R.id.btnSelectApps)
        rvApps = findViewById(R.id.rvApps)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnStopTimer = findViewById(R.id.btnStopTimer)

        rvApps.layoutManager = LinearLayoutManager(this)

        btnSelectApps.setOnClickListener {
            if (rvApps.visibility == View.VISIBLE) {
                rvApps.visibility = View.GONE
            } else {
                rvApps.visibility = View.VISIBLE
            }
        }

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnDeviceAdmin.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }

        btnBatteryOptOut.setOnClickListener {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }

        btnStartTimer.setOnClickListener {
            val mins = etMinutes.text.toString()
            if (mins.isNotEmpty()) {
                Toast.makeText(this, "$mins মিনিটের জন্য টাইমার শুরু হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "মিনিট লিখে দিন", Toast.LENGTH_SHORT).show()
            }
        }

        btnStopTimer.setOnClickListener {
            Toast.makeText(this, "টাইমার বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }
}
