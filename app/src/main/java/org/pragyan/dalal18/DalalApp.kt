package org.pragyan.dalal18

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.pragyan.dalal18.utils.NotificationChannelIds

class DalalApp : Application() {

    override fun onCreate() {
        super.onCreate()

        createPushNotificationsServiceChannel()
    }

    private fun createPushNotificationsServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    NotificationChannelIds.PUSH_NOTIF_SERVICE_CHANNEL,
                    "Push Notification Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
