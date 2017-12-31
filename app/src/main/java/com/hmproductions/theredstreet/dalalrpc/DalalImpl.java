package com.hmproductions.theredstreet.dalalrpc;

import android.util.Log;

import com.hmproductions.theredstreet.DalalGrpc;
import com.hmproductions.theredstreet.StockReply;
import com.hmproductions.theredstreet.StockRequest;

import io.grpc.stub.StreamObserver;

public class DalalImpl extends DalalGrpc.DalalImplBase {

    @Override
    public void sayStock(StockRequest request, StreamObserver<StockReply> responseObserver) {

        Log.v(":::", request.getName());

        String responseString = "Hello there, " + request.getName();

        StockReply reply = StockReply.newBuilder().setMessage(responseString).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
