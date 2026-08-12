package com.focuslock.app

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var etMinutes: TextInputEditText
    private lateinit var btnStartTimer: MaterialButton
    private lateinit var btnStopTimer: MaterialButton
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etMinutes = findViewById(R.id.etMinutes)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnStopTimer = findViewById(R.id.btnStopTimer)
        tvStatus = findViewById(R.id.tvStatus)

        btnStartTimer.setOnClickListener {
            val mins = etMinutes.text.toString()
            if (mins.isNotEmpty()) {
                tvStatus.text = "$mins মিনিটের টাইমার চলছে..."
                Toast.makeText(this, "টাইমার শুরু হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "মিনিট লিখুন", Toast.LENGTH_SHORT).show()
            }
        }

        btnStopTimer.setOnClickListener {
            tvStatus.text = "টাইমার বন্ধ হয়েছে"
            Toast.makeText(this, "টাইমার বন্ধ", Toast.LENGTH_SHORT).show()
        }
    }
}
