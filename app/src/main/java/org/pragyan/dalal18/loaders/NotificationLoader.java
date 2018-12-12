package org.pragyan.dalal18.loaders;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetNotificationsRequest;
import dalalstreet.api.actions.GetNotificationsResponse;

public class NotificationLoader extends AsyncTaskLoader<GetNotificationsResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;
    private int lastId;

    public NotificationLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub,int lastId) {
        super(context);
        this.actionServiceBlockingStub = stub;
        this.lastId = lastId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetNotificationsResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            return actionServiceBlockingStub.getNotifications(
                    GetNotificationsRequest.newBuilder().setLastNotificationId(lastId).setCount(10).build());
        else
            return null;
    }
}
