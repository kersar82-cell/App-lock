package com.focuslock.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.focuslock.app.databinding.ActivityPinBinding

/**
 * সেট-আপ মোডে (isPinSet == false): নতুন PIN তৈরি করায়।
 * ভেরিফাই মোডে: সঠিক PIN দিলে তবেই ACTION_STOP_TIMER কাজ করে অথবা
 * MainActivity-তে ফিরে গিয়ে ব্লকলিস্ট/টাইমার বন্ধ করার অনুমতি দেয়।
 */
class PinActivity : AppCompatActivity() {

    companion object {
        const val ACTION_STOP_TIMER = "com.focuslock.app.ACTION_STOP_TIMER_PIN"
        const val EXTRA_MODE = "mode"
        const val MODE_SETUP = "setup"
        const val MODE_VERIFY = "verify"
    }

    private lateinit var binding: ActivityPinBinding
    private var mode: String = MODE_VERIFY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = if (!PrefsManager.isPinSet(this)) MODE_SETUP
        else intent.getStringExtra(EXTRA_MODE) ?: MODE_VERIFY

        binding.tvTitle.text = if (mode == MODE_SETUP)
            "একটা নতুন PIN সেট করুন" else "চালিয়ে যেতে PIN দিন"

        binding.btnConfirm.setOnClickListener { onConfirm() }
    }

    private fun onConfirm() {
        val pin = binding.etPin.text.toString().trim()
        if (pin.length < 4) {
            Toast.makeText(this, "কমপক্ষে ৪ ডিজিট দিন", Toast.LENGTH_SHORT).show()
            return
        }

        if (mode == MODE_SETUP) {
            PrefsManager.setPin(this, pin)
            Toast.makeText(this, "PIN সেভ হয়েছে", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
            return
        }

        if (PrefsManager.checkPin(this, pin)) {
            if (intent.action == ACTION_STOP_TIMER) {
                startService(Intent(this, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                })
                PrefsManager.stopBlocking(this)
                Toast.makeText(this, "টাইমার বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "PIN ভুল হয়েছে", Toast.LENGTH_SHORT).show()
            binding.etPin.text?.clear()
        }
    }
}
