//package com.hmproductions.theredstreet.loaders;
//
//import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.content.AsyncTaskLoader;
//
//import com.hmproductions.theredstreet.utils.ConnectionUtils;
//import com.hmproductions.theredstreet.utils.Constants;
//
//import dalalstreet.api.DalalActionServiceGrpc;
//
//public class RegistrationLoader extends AsyncTaskLoader<RegistrationResponse> {
//
//    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;
//
//    public RegistrationLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub) {
//        super(context);
//        actionServiceBlockingStub = stub;
//    }
//
//    @Nullable
//    @Override
//    public RegistrationResponse loadInBackground() {
//        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
//            return actionServiceBlockingStub.register(RegistrationRequest.newBuilder().build());
//        else
//            return null;
//    }
//}
