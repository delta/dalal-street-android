package org.pragyan.dalal18.notifications

import android.app.Service
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
import android.app.PendingIntent
import android.content.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.pragyan.dalal18.R
import org.pragyan.dalal18.ui.SplashActivity


class PushNotificationService : Service() {
    private val TAG = "PushNotificationService"
    private lateinit var subscriptionId: SubscriptionId
    private lateinit var marketSubscriptionId: SubscriptionId

    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    @Inject
    lateinit var streamServiceBlockingStub: DalalStreamServiceGrpc.DalalStreamServiceBlockingStub

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    var isLoggedIn = true

    private val stopNotificationServiceBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isLoggedIn = false
            unsubscribeFromNotificationStream()
            unsubscribeFromMarketEventStream()
        }
    }

    override fun onCreate() {
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        LocalBroadcastManager.getInstance(this).registerReceiver(stopNotificationServiceBroadcast, IntentFilter(Constants.STOP_NOTIFICATION_ACTION))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (isLoggedIn) {
            Intent().also { intent ->
                intent.setAction("android.intent.action.NotifServiceBroadcast")
                sendImplicitBroadcast(applicationContext, intent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subscribeToStreamsAsynchronously()
//        Uncomment next line to test if service is running
//        debugService()
        return START_STICKY
    }


    private fun subscribeToStreamsAsynchronously() = doAsync {
        if (ConnectionUtils.getConnectionInfo(this@PushNotificationService))
            if (!ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
                Log.d(TAG, "Not Reachable")

        doAsync {
            val notificationsResponse = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.NOTIFICATIONS).setDataStreamId("").build())
            subscriptionId = notificationsResponse.subscriptionId
            subscribeToNotificationsStream(notificationsResponse.subscriptionId)

            val marketEventResponse = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MARKET_EVENTS).setDataStreamId("").build())
            marketSubscriptionId = marketEventResponse.subscriptionId

            subscribeToMarketsStream(marketEventResponse.subscriptionId)

        }

    }

    private fun subscribeToMarketsStream(subscriptionId: SubscriptionId?) {

        streamServiceStub.getMarketEventUpdates(subscriptionId,
                object : StreamObserver<MarketEventUpdate> {
                    override fun onNext(value: MarketEventUpdate) {
                        val event = value.marketEvent
                        val builder = NotificationCompat.Builder(applicationContext, getString(R.string.notification_channel_id))
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

    private fun unsubscribeFromMarketEventStream() {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@PushNotificationService) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val unsubscribeResponse = streamServiceBlockingStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(marketSubscriptionId).build())

            }
        }
    }

    private fun debugService() {
        doAsync {
            for (i in 1..100) {
                Thread.sleep(1000)
                Log.d("Alive", "Service is Alive" + i.toString())
            }

        }
    }

    private fun subscribeToNotificationsStream(notificationsSubscriptionId: SubscriptionId) {

        streamServiceStub.getNotificationUpdates(notificationsSubscriptionId,
                object : StreamObserver<NotificationUpdate> {
                    override fun onNext(value: NotificationUpdate) {
                        val notification = value.notification
                        val myIntent = Intent(applicationContext, SplashActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        val builder = NotificationCompat.Builder(applicationContext, getString(R.string.notification_channel_id))
                                .setSmallIcon(R.drawable.market_depth_icon)
                                .setAutoCancel(true)
                                .setContentText(notification.text)
                                .setContentTitle("Dalal Street")
                                .setContentIntent(pendingIntent)
                                .build()
                        notificationManager.notify(notification.id, builder)

                    }

                    override fun onError(t: Throwable?) {
                    }

                    override fun onCompleted() {
                    }

                })
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
