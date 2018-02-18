package org.pragyan.dalal18.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetLeaderboardRequest;
import dalalstreet.api.actions.GetLeaderboardResponse;

public class LeaderBoardLoader extends AsyncTaskLoader<GetLeaderboardResponse> {

    private static final int LEADERBOARD_SIZE = 15;

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    public LeaderBoardLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub) {
        super(context);
        this.actionServiceBlockingStub = actionServiceBlockingStub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetLeaderboardResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            return actionServiceBlockingStub.getLeaderboard(
                    GetLeaderboardRequest
                            .newBuilder()
                            .setCount(LEADERBOARD_SIZE)
                            .setStartingId(1)
                            .build()
            );
        else
            return null;
    }
}
