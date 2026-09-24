package com.berkeh.connector

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var lastSmsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(40), dp(24), dp(24))
        }

        val title = TextView(this).apply {
            text = "Berkeh Connector"
            textSize = 26f
        }

        statusText = TextView(this).apply {
            textSize = 16f
            setPadding(0, dp(24), 0, dp(16))
        }

        val permissionButton = Button(this).apply {
            text = "فعال‌سازی دسترسی پیامک"
            setOnClickListener { requestSmsPermission() }
        }

        lastSmsText = TextView(this).apply {
            textSize = 15f
            setPadding(0, dp(28), 0, 0)
        }

        root.addView(title)
        root.addView(statusText)
        root.addView(permissionButton)
        root.addView(lastSmsText)
        setContentView(root)

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun requestSmsPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            ),
            1001
        )
    }

    private fun refreshUi() {
        val granted =
            checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED

        statusText.text =
            if (granted) "✓ دسترسی SMS فعال است" else "دسترسی SMS هنوز فعال نشده"

        val prefs = getSharedPreferences("berkeh_connector", MODE_PRIVATE)
        val bank = prefs.getString("last_bank", null)
        val amount = prefs.getString("last_amount", null)
        val sender = prefs.getString("last_sender", null)
        val type = prefs.getString("last_type", null)

        lastSmsText.text = if (bank == null) {
            "هنوز پیام بانکی تشخیص داده نشده است."
        } else {
            buildString {
                append("آخرین پیام بانکی\n\n")
                append("بانک: ").append(bank).append('\n')
                append("نوع: ").append(type ?: "نامشخص").append('\n')
                append("مبلغ: ").append(amount ?: "نامشخص").append('\n')
                append("فرستنده: ").append(sender ?: "نامشخص")
            }
        }
    }
}
