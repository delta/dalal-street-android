package com.hmproductions.theredstreet.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("SAN","broadcast called");
        context.startService(new Intent(context,NotificationService.class));
    }
}
