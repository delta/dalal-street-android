package com.hmproductions.theredstreet.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadCastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context,NotificationService.class));
    }
}
