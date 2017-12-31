package com.hmproductions.theredstreet.dalalrpc;

import android.util.Log;

import com.hmproductions.theredstreet.DalalGrpc;
import com.hmproductions.theredstreet.StockReply;
import com.hmproductions.theredstreet.StockRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DalalGrpcClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public DalalGrpcClient() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext(true)
                .build();

        DalalGrpc.DalalBlockingStub stub = DalalGrpc.newBlockingStub(channel);

        StockRequest request = StockRequest.newBuilder()
                .setName("Harsh")
                .build();

        StockReply reply = stub.sayStock(request);

        Log.v(":::", reply.getMessage());

        channel.shutdown();
    }
}
