package com.hmproductions.theredstreet.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import io.grpc.ManagedChannel;

public class LoginLoader extends AsyncTaskLoader<LoginResponse> {

    private LoginRequest loginRequest;
    private DalalActionServiceGrpc.DalalActionServiceBlockingStub stub;

    public LoginLoader(Context context, LoginRequest loginRequest, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub) {
        super(context);
        this.loginRequest = loginRequest;
        this.stub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public LoginResponse loadInBackground() {

        LoginResponse loginResponse = stub.login(loginRequest);

        Log.v(":::", "authenticated as " + loginResponse.getUser().getName() + "session id" + loginResponse.getSessionId() + "status code = " + String.valueOf(loginResponse.getStatusCode()));

        if (stub != null) {
            ((ManagedChannel)stub.getChannel()).shutdown();
        }

        return loginResponse;
    }

}
