package com.hmproductions.theredstreet.notifications;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.ui.SplashActivity;
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;

import javax.inject.Inject;

import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.NotificationUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.models.Notification;
import io.grpc.stub.StreamObserver;

public class NotificationService extends IntentService {

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

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.v(":::", "on start command called");
        onHandleIntent(intent);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        Log.v(":::", "starting service ");
        buildNotification();

        if (preferences.getBoolean(MainActivity.USER_LOGGED_IN, false)) {

            SubscribeResponse subscribeResponse = streamServiceBlockingStub.
                    subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.NOTIFICATIONS).setDataStreamId("").build());


            streamServiceStub.getNotificationUpdates(subscribeResponse.getSubscriptionId(),

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
                                notificationManager.notify(NOTIFICATION_ID, builder.build());
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
}
