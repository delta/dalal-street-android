package org.pragyan.dalal18.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.doAsync

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        doAsync {
            //HACK TO KEEP THE SERVICE ALIVE
            //This Just gets called to keep the thread Alive. Nothing has to be done here.
        }
    }
}
