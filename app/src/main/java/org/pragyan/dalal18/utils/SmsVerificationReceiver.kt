package org.pragyan.dalal18.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.pragyan.dalal18.utils.Constants.SMS_KEY

class SmsVerificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String

                    Log.v(SmsVerificationReceiver::class.java.simpleName, message)

                    val newSmsIntent = Intent(Constants.NEW_SMS_RECEIVED_ACTION)
                    newSmsIntent.putExtra(SMS_KEY, message)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(newSmsIntent)
                }
                CommonStatusCodes.TIMEOUT -> {

                }
            }
        }
    }
}