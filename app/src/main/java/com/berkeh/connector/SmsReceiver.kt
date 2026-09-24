package com.berkeh.connector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val sender = messages.firstOrNull()?.originatingAddress.orEmpty()
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }.trim()
        if (body.isBlank()) return

        val bank = detectBank(sender, body) ?: return
        val type = detectType(body)
        if (type == "برداشت") return

        val amount = extractAmount(body)

        context.getSharedPreferences("berkeh_connector", Context.MODE_PRIVATE)
            .edit()
            .putString("last_bank", bank)
            .putString("last_type", type)
            .putString("last_amount", amount ?: "نامشخص")
            .putString("last_sender", sender)
            .putString("last_body", body)
            .putLong("last_time", System.currentTimeMillis())
            .apply()
    }

    private fun detectBank(sender: String, body: String): String? {
        val text = normalize(sender + " " + body).lowercase()

        val banks = linkedMapOf(
            "بانک ایران زمین" to listOf("ایران زمین", "ايران زمين", "iranzamin", "iran zamin"),
            "بانک ملت" to listOf("بانک ملت", "بانك ملت", "mellat"),
            "بانک ملی" to listOf("بانک ملی", "بانك ملي", "bank melli", "melli"),
            "بانک تجارت" to listOf("بانک تجارت", "بانك تجارت", "tejarat"),
            "بانک صادرات" to listOf("بانک صادرات", "بانك صادرات", "saderat"),
            "بانک سپه" to listOf("بانک سپه", "بانك سپه", "sepah"),
            "بانک سامان" to listOf("بانک سامان", "بانك سامان", "saman")
        )

        return banks.entries.firstOrNull { (_, keys) ->
            keys.any { text.contains(normalize(it).lowercase()) }
        }?.key
    }

    private fun detectType(body: String): String {
        val text = normalize(body)

        val debitWords = listOf("برداشت", "خرید", "کسر", "بدهکار", "بدهكار")
        if (debitWords.any { text.contains(it, ignoreCase = true) }) return "برداشت"

        val creditWords = listOf(
            "واریز", "واريز", "بستانکار", "بستانكار",
            "دریافت", "دريافت", "واریزی", "واريزي"
        )
        if (creditWords.any { text.contains(it, ignoreCase = true) }) return "واریز"

        return "نامشخص"
    }

    private fun extractAmount(body: String): String? {
        val normalized = normalizeDigits(body)
            .replace("٬", ",")
            .replace("،", ",")

        val patterns = listOf(
            Regex("""(?:مبلغ|واریز|واريز|بستانکار|بستانكار|دریافت|دريافت)\D{0,25}([0-9][0-9,\s]{2,})"""),
            Regex("""([0-9][0-9,\s]{3,})\s*(?:ریال|ريال|تومان)""")
        )

        for (pattern in patterns) {
            val value = pattern.find(normalized)?.groupValues?.getOrNull(1)
                ?.replace(",", "")
                ?.replace(" ", "")
                ?.trim()
            if (!value.isNullOrBlank() && value.all { it.isDigit() }) return value
        }

        return null
    }

    private fun normalizeDigits(value: String): String {
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        val out = StringBuilder()

        value.forEach { ch ->
            val p = persian.indexOf(ch)
            val a = arabic.indexOf(ch)
            when {
                p >= 0 -> out.append(('0'.code + p).toChar())
                a >= 0 -> out.append(('0'.code + a).toChar())
                else -> out.append(ch)
            }
        }
        return out.toString()
    }

    private fun normalize(value: String): String {
        return normalizeDigits(value)
            .replace('ي', 'ی')
            .replace('ك', 'ک')
    }
}
