package com.hmproductions.theredstreet.dagger;

import com.hmproductions.theredstreet.MiscellaneousUtils;

import dagger.Module;
import dagger.Provides;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;

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

    @Provides
    @DalalStreetApplicationScope
    public Metadata getStubMetadata() {
        Metadata metadata = new Metadata();
        Metadata.Key<String> metadataKey = Metadata.Key.of("session_id", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(metadataKey, MiscellaneousUtils.sessionId);
        return metadata;
    }
}
