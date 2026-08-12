package com.focuslock.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        val prefs = getSharedPreferences("FocusLockPrefs", MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)
        val etPin = findViewById<EditText>(R.id.etPin)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val inputPin = etPin.text.toString()
            if (savedPin == null) {
                // পিন সেট করা নেই, প্রথমবার সেট করুন
                prefs.edit().putString("user_pin", inputPin).apply()
                Toast.makeText(this, "পিন সেট হয়েছে!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (savedPin == inputPin) {
                // সঠিক পিন
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "ভুল পিন!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
