package com.hmproductions.theredstreet.dagger;

import com.hmproductions.theredstreet.MiscellaneousUtils;

import dagger.Module;
import dagger.Provides;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

@Module (includes = ContextModule.class)
public class StubModule {

    @Provides
    @DalalStreetApplicationScope
    public DalalActionServiceGrpc.DalalActionServiceStub getDalalActionStub(ManagedChannel channel, Metadata metadata) {
        DalalActionServiceGrpc.DalalActionServiceStub stub = DalalActionServiceGrpc.newStub(channel);
        MetadataUtils.attachHeaders(stub, metadata);
        return stub;
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalActionServiceGrpc.DalalActionServiceBlockingStub getDalalActionBlockingStub(ManagedChannel channel, Metadata metadata) {
        DalalActionServiceGrpc.DalalActionServiceBlockingStub stub = DalalActionServiceGrpc.newBlockingStub(channel);
        MetadataUtils.attachHeaders(stub, metadata);
        return stub;
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalStreamServiceGrpc.DalalStreamServiceStub getDalalStreamStub(ManagedChannel channel, Metadata metadata) {
        DalalStreamServiceGrpc.DalalStreamServiceStub stub = DalalStreamServiceGrpc.newStub(channel);
        MetadataUtils.attachHeaders(stub, metadata);
        return stub;
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalStreamServiceGrpc.DalalStreamServiceBlockingStub getDalalStreamBlockingStub(ManagedChannel channel, Metadata metadata) {
        DalalStreamServiceGrpc.DalalStreamServiceBlockingStub stub = DalalStreamServiceGrpc.newBlockingStub(channel);
        MetadataUtils.attachHeaders(stub, metadata);
        return stub;
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
