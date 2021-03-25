package org.pragyan.dalal18.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService: FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        // modify this function as per need
        // you can start services from here and stop them as well
        Log.d("FCM-MESSAGE", p0.notification?.body.toString())
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}