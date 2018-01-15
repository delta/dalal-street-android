package com.hmproductions.theredstreet.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;

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
        return stub.login(loginRequest);
    }
}