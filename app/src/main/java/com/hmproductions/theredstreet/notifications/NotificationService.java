package com.hmproductions.theredstreet.notifications;

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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.ui.SplashActivity;
import com.hmproductions.theredstreet.utils.Constants;

import javax.inject.Inject;

import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.NotificationUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.models.Notification;
import io.grpc.stub.StreamObserver;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "miscellaneous";
    private static final int RC_NOTIF_CLICK = 19;
    private static final int NOTIFICATION_ID = 3;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceBlockingStub streamServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @Inject
    SharedPreferences preferences;

    private NotificationCompat.Builder builder = null;
    SubscriptionId subscriptionId;
    boolean isLoggedIn = true;

    private BroadcastReceiver stopNotifBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isLoggedIn = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.STOP_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(stopNotifBroadcast, intentFilter);

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        onHandleIntent();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void onHandleIntent() {

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        buildNotification();

        streamServiceStub.
                subscribe(SubscribeRequest.newBuilder()
                        .setDataStreamType(DataStreamType.NOTIFICATIONS)
                        .setDataStreamId("")
                        .build(), new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0) {
                            subscriptionId = value.getSubscriptionId();
                            subscribeNotif(subscriptionId);
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

    private void subscribeNotif(SubscriptionId subscriptionId) {

        streamServiceStub.getNotificationUpdates(subscriptionId,

                new StreamObserver<NotificationUpdate>() {
                    @Override
                    public void onNext(NotificationUpdate value) {

                        Notification notification = value.getNotification();

                        if (notification.getText().equals(preferences.getString(Constants.MARKET_OPEN_TEXT_KEY, null))) {
                            builder.setContentTitle("Market Open")
                                    .setContentText("Dalal Street market has opened just now !");
                        } else if (notification.getText().equals(preferences.getString(Constants.MARKET_CLOSED_TEXT_KEY, null))) {
                            builder.setContentTitle("Market Closed")
                                    .setContentText("Market has closed now.");
                        } else {
                            builder.setContentTitle("Event Update")
                                    .setContentText(notification.getText());
                        }

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
            restartServiceTask.setPackage(getPackageName());
            PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
            myAlarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 100,
                    restartPendingIntent);
        }
        super.onTaskRemoved(rootIntent);
    }
}
