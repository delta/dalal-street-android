package org.pragyan.dalal18.fcm

import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.pragyan.dalal18.R

class FCMService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: " + remoteMessage!!.from)
        Log.d(TAG, "Notification Message Body: " + remoteMessage.notification?.body!!)
        sendNotification(remoteMessage)
        Log.d("FCM-MESSAGE", remoteMessage.notification?.body.toString())
    }
    private fun sendNotification(remoteMessage: RemoteMessage) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setContentText(remoteMessage.notification?.body)
                .setAutoCancel(true)
                .setContentTitle(remoteMessage.notification?.title)
                .setSmallIcon(R.mipmap.dalal_icon21)
                .setSound(defaultSoundUri)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}