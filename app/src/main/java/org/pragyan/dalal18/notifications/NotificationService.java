package org.pragyan.dalal18.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.ui.SplashActivity;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.TinyDB;

import java.util.ArrayList;

import javax.inject.Inject;

import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.MarketEventUpdate;
import dalalstreet.api.datastreams.NotificationUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.models.MarketEvent;
import dalalstreet.api.models.Notification;
import io.grpc.stub.StreamObserver;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "miscellaneous";
    private static final int RC_NOTIF_CLICK = 19;
    private static final int NOTIFICATION_ID = 3;
    private static final int NEWS_NOTIFICATION_ID = 4;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @Inject
    SharedPreferences preferences;

    private NotificationCompat.Builder builder = null;
    NotificationManager notificationManager;
    SubscriptionId subscriptionId,newsSubscriptionId;
    boolean isLoggedIn = true;
    NotificationCompat.InboxStyle style;
    TinyDB tinyDB;
    ArrayList<String> notifList = new ArrayList<>();
    ArrayList<String> notificationNewsList = new ArrayList<>();

    private BroadcastReceiver stopNotificationServiceBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isLoggedIn = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.STOP_NOTIFICATION_ACTION);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(stopNotificationServiceBroadcast, intentFilter);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        tinyDB = new TinyDB(this);
        startSubscription();
        startNewsSubscrition();

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void startNewsSubscrition() {

        streamServiceStub.
                subscribe(SubscribeRequest.newBuilder()
                        .setDataStreamType(DataStreamType.MARKET_EVENTS)
                        .setDataStreamId("")
                        .build(), new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0) {
                            newsSubscriptionId = value.getSubscriptionId();
                            subscribeToNewsStream(newsSubscriptionId);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {


                    }
                });
    }

    private void subscribeToNewsStream(SubscriptionId newsSubscriptionId) {

        buildNotification();
        streamServiceStub.getMarketEventUpdates(newsSubscriptionId, new StreamObserver<MarketEventUpdate>() {
            @Override
            public void onNext(MarketEventUpdate value) {
                MarketEvent event = value.getMarketEvent();

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                notificationNewsList = tinyDB.getListString(Constants.NOTIFICATION_NEWS_SHARED_PREF);

                builder.setContentTitle("Dalal News")
                        .setContentText(event.getHeadline());
                notificationNewsList.add(event.getHeadline());

                if(notificationNewsList.size() > 1){

                    inboxStyle.setBigContentTitle("Dalal News");
                    builder.setStyle(inboxStyle);
                    builder.setContentTitle("Dalal News");
                    builder.setContentText("+" + notificationNewsList.size() + " more...");

                    for(int i=0 ; i<notificationNewsList.size() ; i++){
                        inboxStyle.addLine(notificationNewsList.get(i));
                    }
                }

                tinyDB.putListString(Constants.NOTIFICATION_NEWS_SHARED_PREF,notificationNewsList);

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
                    builder.setChannelId(NotificationChannel.DEFAULT_CHANNEL_ID);
                    NotificationChannel notificationChannel = new NotificationChannel(
                            NotificationChannel.DEFAULT_CHANNEL_ID,
                            getString(R.string.dalal_street_notifications),
                            NotificationManager.IMPORTANCE_DEFAULT);

                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(R.color.neon_green);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{100, 200, 400});
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                if (notificationManager != null) {
                    if(isLoggedIn){
                        notificationManager.notify(NEWS_NOTIFICATION_ID, builder.build());
                    }
                }

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    protected void startSubscription() {

        streamServiceStub.
                subscribe(SubscribeRequest.newBuilder()
                        .setDataStreamType(DataStreamType.NOTIFICATIONS)
                        .setDataStreamId("")
                        .build(), new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0) {
                            subscriptionId = value.getSubscriptionId();
                            subscribeToNotificationsStream(subscriptionId);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });


    }

    private void subscribeToNotificationsStream(SubscriptionId subscriptionId) {

        buildNotification();

        streamServiceStub.getNotificationUpdates(subscriptionId,

                new StreamObserver<NotificationUpdate>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onNext(NotificationUpdate value) {

                        Notification notification = value.getNotification();

                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                        notifList = tinyDB.getListString(Constants.NOTIFICATION_SHARED_PREF);

                        if (notification.getText().equals(preferences.getString(Constants.MARKET_OPEN_TEXT_KEY, null))) {
                            if(notifList.size() == 0){
                                builder.setContentTitle("Market Open")
                                        .setContentText("Dalal Street market has opened just now !");
                            }
                            notifList.add("Market Open");
                        } else if (notification.getText().equals(preferences.getString(Constants.MARKET_CLOSED_TEXT_KEY, null))) {
                            if(notifList.size() == 0){
                                builder.setContentTitle("Market Closed")
                                        .setContentText("Market has closed now.");
                            }
                            notifList.add("Market Close");
                        } else {
                            if(notifList.size() == 0){
                                builder.setContentTitle("Event Update")
                                        .setContentText(notification.getText());
                            }
                            notifList.add(notification.getText());
                        }

                        if(notifList.size() > 1){

                            inboxStyle.setBigContentTitle("Dalal Notifications");
                            builder.setStyle(inboxStyle);
                            builder.setContentTitle("Dalal Notifications");
                            builder.setContentText("+" + notifList.size() + " more...");

                            for(int i=0 ; i<notifList.size() ; i++){
                                inboxStyle.addLine(notifList.get(i));
                            }
                        }

                        tinyDB.putListString(Constants.NOTIFICATION_SHARED_PREF,notifList);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
                            builder.setChannelId(NotificationChannel.DEFAULT_CHANNEL_ID);
                            NotificationChannel notificationChannel = new NotificationChannel(
                                    NotificationChannel.DEFAULT_CHANNEL_ID,
                                    getString(R.string.dalal_street_notifications),
                                    NotificationManager.IMPORTANCE_DEFAULT);

                            notificationChannel.enableLights(true);
                            notificationChannel.setLightColor(R.color.neon_green);
                            notificationChannel.enableVibration(true);
                            notificationChannel.setVibrationPattern(new long[]{100, 200, 400});
                            notificationManager.createNotificationChannel(notificationChannel);
                        }

                        if (notificationManager != null) {
                            if(isLoggedIn){
                                notificationManager.notify(NOTIFICATION_ID, builder.build());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

    }

    private void buildNotification() {

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(contentIntent())
                .setSmallIcon(R.drawable.market_depth_icon)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(style)
                .setAutoCancel(true);
    }

    private PendingIntent contentIntent() {
        return PendingIntent.getActivity(this, RC_NOTIF_CLICK, new Intent(this, SplashActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isLoggedIn){
            Intent broadcastIntent = new Intent("NotifServiceBroadcast");
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if(isLoggedIn){
            Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
            //restartServiceTask.setPackage(getPackageName());
            PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(),
                    1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
            if (myAlarmService != null) {
                myAlarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500,
                            restartPendingIntent);
            }
        }
        super.onTaskRemoved(rootIntent);
    }
}