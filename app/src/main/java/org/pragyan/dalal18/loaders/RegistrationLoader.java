package org.pragyan.dalal18.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import org.pragyan.dalal18.data.RegistrationDetails;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.Register;

public class RegistrationLoader extends AsyncTaskLoader<Register.RegisterResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;
    private RegistrationDetails registrationDetails;

    public RegistrationLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub, 
                              RegistrationDetails registrationDetails) {
        super(context);
        actionServiceBlockingStub = stub;
        this.registrationDetails = registrationDetails;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public Register.RegisterResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
            return actionServiceBlockingStub.register(
                    Register.RegisterRequest.newBuilder()
                            .setCountry(registrationDetails.getCountry())
                            .setEmail(registrationDetails.getEmail())
                            .setFullName(registrationDetails.getFullName())
                            .setPassword(registrationDetails.getPassword())
                            .setUserName(registrationDetails.getUsername())
                            .build());
        }
        else
            return null;
    }
}
