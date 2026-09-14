package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PendingTransactionEntity
import com.example.data.local.TransactionType
import com.example.util.BankTransactionType
import com.example.util.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BankSmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.messageBody
                val sender = sms.displayOriginatingAddress
                
                val parsed = SmsParser.parse(body, sender)
                if (parsed != null) {
                    Log.d("BankSmsReceiver", "Parsed SMS: $parsed")
                    saveToPending(context, parsed, body)
                }
            }
        }
    }

    private fun saveToPending(context: Context, parsed: com.example.util.ParsedBankSms, raw: String) {
        val database = AppDatabase.getDatabase(context)
        scope.launch {
            val entity = PendingTransactionEntity(
                title = "تراکنش ${parsed.bankName}",
                amount = parsed.amount,
                type = if (parsed.type == BankTransactionType.DEPOSIT) TransactionType.DEPOSIT else TransactionType.EXPENSE,
                category = "بانکی",
                timestamp = System.currentTimeMillis(),
                rawSmsContent = raw,
                bankName = parsed.bankName
            )
            database.pendingTransactionDao().insertPending(entity)
        }
    }
}
