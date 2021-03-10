package org.pragyan.dalal18.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        GlobalScope.async (Dispatchers.Default){
            //HACK TO KEEP THE SERVICE ALIVE
            //This Just gets called to keep the thread Alive. Nothing has to be done here.
        }
    }
}
