package com.hmproductions.theredstreet.dagger;

import dagger.Module;
import dagger.Provides;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import io.grpc.ManagedChannel;

@Module (includes = ContextModule.class)
public class StubModule {

    @Provides
    @DalalStreetApplicationScope
    public DalalActionServiceGrpc.DalalActionServiceStub getDalalActionStub(ManagedChannel channel) {
        return DalalActionServiceGrpc.newStub(channel);
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalActionServiceGrpc.DalalActionServiceBlockingStub getDalalActionBlockingStub(ManagedChannel channel) {
        return DalalActionServiceGrpc.newBlockingStub(channel);
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalStreamServiceGrpc.DalalStreamServiceStub getDalalStreamStub(ManagedChannel channel) {
        return DalalStreamServiceGrpc.newStub(channel);
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalStreamServiceGrpc.DalalStreamServiceBlockingStub getDalalStreamBlockingStub(ManagedChannel channel) {
        return DalalStreamServiceGrpc.newBlockingStub(channel);
    }
}
