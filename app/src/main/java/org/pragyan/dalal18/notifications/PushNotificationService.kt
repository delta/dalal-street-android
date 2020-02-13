package org.pragyan.dalal18.notifications

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.datastreams.*
import io.grpc.stub.StreamObserver
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import javax.inject.Inject
import android.app.NotificationManager
import android.content.Context


class PushNotificationService : Service() {

    lateinit var notificationmanager: NotificationManager

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    @Inject
    lateinit var streamServiceBlockingStub: DalalStreamServiceGrpc.DalalStreamServiceBlockingStub

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate() {
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)
        notificationmanager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAGDAAW", "Big F1")
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        createNetworkCallbackObject()
        subscribeToStreamsAsynchronously()
        return super.onStartCommand(intent, flags, startId)
    }


    private fun subscribeToStreamsAsynchronously() = doAsync {
        Log.d("TAGDAAW", "Big F2")
                if(ConnectionUtils.getConnectionInfo(this@PushNotificationService))
        Log.d("TAGDAAW", "Big F3")
                if(!ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
                    Log.d("TAGDAAW", "Not Reachable")

            doAsync {
                val notificationsResponse = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.NOTIFICATIONS).setDataStreamId("").build())
                subscribeToNotificationsStream(notificationsResponse.subscriptionId)
                Log.d("TAGDAAW", "Big F4")
            }

    }

    private fun subscribeToNotificationsStream(notificationsSubscriptionId: SubscriptionId) {

        streamServiceStub.getNotificationUpdates(notificationsSubscriptionId,
                object : StreamObserver<NotificationUpdate> {
                    override fun onNext(value: NotificationUpdate) {

                        val notification = value.notification

                        Log.d("TAGDAAW", notification.text)
//                        var builder = NotificationCompat.Builder(this@PushNotificationService, "test_channel_01")
//                                .setContentText(notification.text)
//                                .setContentTitle("Dalal Street")
//                                .build()
//                        notificationmanager.notify(notification.id, builder)

                    }

                    override fun onError(t: Throwable?) {
                    }

                    override fun onCompleted() {
                    }

                })
    }

    private fun createNetworkCallbackObject() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                doAsync {
                    if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                        uiThread {

                        }
                    } else {
                        Log.d("TAGDAAW", "Error in createNetworkCallbackObject")
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


}
