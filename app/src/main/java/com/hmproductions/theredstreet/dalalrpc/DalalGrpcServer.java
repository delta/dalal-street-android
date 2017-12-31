package com.hmproductions.theredstreet.dalalrpc;

import android.util.Log;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class DalalGrpcServer {

    private static final int SERVER_PORT = 8080;

    public DalalGrpcServer() {

        Server server = ServerBuilder.forPort(SERVER_PORT)
                .addService(new DalalImpl())
                .build();

        try {

            Log.v(":::", "Server started on port : " + String.valueOf(SERVER_PORT));
            server.start();

            server.awaitTermination();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
