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
    public DalalActionServiceGrpc.DalalActionServiceStub getDalalActionStub(ManagedChannel channel) {
        DalalActionServiceGrpc.DalalActionServiceStub stub = DalalActionServiceGrpc.newStub(channel);
        stub = MetadataUtils.attachHeaders(stub, getStubMetadata());
        return stub;
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalActionServiceGrpc.DalalActionServiceBlockingStub getDalalActionBlockingStub(ManagedChannel channel) {
        DalalActionServiceGrpc.DalalActionServiceBlockingStub stub = DalalActionServiceGrpc.newBlockingStub(channel);
        stub = MetadataUtils.attachHeaders(stub, getStubMetadata());
        return stub;
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalStreamServiceGrpc.DalalStreamServiceStub getDalalStreamStub(ManagedChannel channel) {
        DalalStreamServiceGrpc.DalalStreamServiceStub stub = DalalStreamServiceGrpc.newStub(channel);
        stub = MetadataUtils.attachHeaders(stub, getStubMetadata());
        return stub;
    }

    @Provides
    @DalalStreetApplicationScope
    public DalalStreamServiceGrpc.DalalStreamServiceBlockingStub getDalalStreamBlockingStub(ManagedChannel channel) {
        DalalStreamServiceGrpc.DalalStreamServiceBlockingStub stub = DalalStreamServiceGrpc.newBlockingStub(channel);
        stub = MetadataUtils.attachHeaders(stub, getStubMetadata());
        return stub;
    }
    
    private Metadata getStubMetadata() {
        Metadata metadata = new Metadata();
        Metadata.Key<String> metadataKey = Metadata.Key.of("sessionid", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(metadataKey, MiscellaneousUtils.sessionId);
        return metadata;
    }
}
