package org.pragyan.dalal18.notifications

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.IBinder
import android.util.Log
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
import android.content.ComponentName
import org.pragyan.dalal18.R


class PushNotificationService : Service() {
    lateinit var subscriptionId: SubscriptionId
    lateinit var marketSubscriptionId: SubscriptionId

    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    @Inject
    lateinit var streamServiceBlockingStub: DalalStreamServiceGrpc.DalalStreamServiceBlockingStub

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate() {
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
//        Log.d("TAGDAAW", "Task Removed")
        Intent().also { intent ->
            intent.setAction("android.intent.action.NotifServiceBroadcast")
            sendImplicitBroadcast(applicationContext, intent)
        }
//        Log.d("TAGDAAW", "Intent Sent")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d("TAGDAAW", "Big F1")
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        createNetworkCallbackObject()
        subscribeToStreamsAsynchronously()
        debu()
        return START_STICKY
    }


    private fun subscribeToStreamsAsynchronously() = doAsync {
//        Log.d("TAGDAAW", "Big F2")
                if(ConnectionUtils.getConnectionInfo(this@PushNotificationService))
//        Log.d("TAGDAAW", "Big F3")
                if(!ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
                    Log.d("TAGDAAW", "Not Reachable")

            doAsync {
                val notificationsResponse = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.NOTIFICATIONS).setDataStreamId("").build())
                subscriptionId = notificationsResponse.subscriptionId
//                Log.d("WAKANDA", notificationsResponse.subscriptionId.toString())
                subscribeToNotificationsStream(notificationsResponse.subscriptionId)

                val marketEventResponse = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MARKET_EVENTS).setDataStreamId("").build())
                marketSubscriptionId = marketEventResponse.subscriptionId

                subscribeToMarketsStream(marketEventResponse.subscriptionId)

//                Log.d("TAGDAAW", "Big F4")
            }

    }

    private fun subscribeToMarketsStream(subscriptionId: SubscriptionId?) {

        streamServiceStub.getMarketEventUpdates(subscriptionId,
                object : StreamObserver<MarketEventUpdate> {
                    override fun onNext(value: MarketEventUpdate) {
//                        Log.d("TAGDAAW", "hello")
                        val event = value.marketEvent

//                        Log.d("TAGDAAW", notification.text)
                        var builder = NotificationCompat.Builder(applicationContext, "dalal_notification_channel")
                                .setSmallIcon(R.drawable.market_depth_icon)
                                // todo change icon if needed
                                .setAutoCancel(true)
                                .setContentText(event.headline)
                                .setContentTitle("Dalal Street")
                                .build()
                        notificationManager.notify(event.id, builder)
                    }

                    override fun onError(t: Throwable?) {
                    }

                    override fun onCompleted() {
                    }

                })
    }

    private fun unsubscribeFromNotificationStream() {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@PushNotificationService) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val unsubscribeResponse = streamServiceBlockingStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(subscriptionId).build())

            }
        }
    }
    private fun debu(){
        doAsync {
            for(i in 1..100){
                Thread.sleep(1000)
//                Log.d("Alive", "Service is Alive" + i.toString())
            }

        }
    }
    private fun subscribeToNotificationsStream(notificationsSubscriptionId: SubscriptionId) {

        streamServiceStub.getNotificationUpdates(notificationsSubscriptionId,
                object : StreamObserver<NotificationUpdate> {
                    override fun onNext(value: NotificationUpdate) {
//                        Log.d("TAGDAAW", "hello")
                        val notification = value.notification

//                        Log.d("TAGDAAW", notification.text)
                        var builder = NotificationCompat.Builder(applicationContext, "dalal_notification_channel")
                                .setSmallIcon(R.drawable.market_depth_icon)
                                .setAutoCancel(true)
                                .setContentText(notification.text)
                                .setContentTitle("Dalal Street")
                                .build()
                        notificationManager.notify(notification.id, builder)

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
//                        Log.d("TAGDAAW", "Error in createNetworkCallbackObject")
                    }
                }
            }
        }
    }

    private fun sendImplicitBroadcast(ctxt: Context, i: Intent) {
        val pm = ctxt.packageManager
        val matches = pm.queryBroadcastReceivers(i, 0)

        for (resolveInfo in matches) {
            val explicit = Intent(i)
            val cn = ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name)

            explicit.component = cn
            ctxt.sendBroadcast(explicit)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


}
