package dalalstreet.api;

import dalalstreet.api.datastreams.MarketDepthUpdate;
import dalalstreet.api.datastreams.MarketEventUpdate;
import dalalstreet.api.datastreams.MyOrderUpdate;
import dalalstreet.api.datastreams.NotificationUpdate;
import dalalstreet.api.datastreams.StockExchangeUpdate;
import dalalstreet.api.datastreams.StockPricesUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.datastreams.TransactionUpdate;
import dalalstreet.api.datastreams.UnsubscribeRequest;
import dalalstreet.api.datastreams.UnsubscribeResponse;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.8.0)",
    comments = "Source: DalalMessage.proto")
public final class DalalStreamServiceGrpc {

  private DalalStreamServiceGrpc() {}

  public static final String SERVICE_NAME = "proto.DalalStreamService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getSubscribeMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscribeRequest,
      SubscribeResponse> METHOD_SUBSCRIBE = getSubscribeMethod();

  private static volatile io.grpc.MethodDescriptor<SubscribeRequest,
      SubscribeResponse> getSubscribeMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscribeRequest,
      SubscribeResponse> getSubscribeMethod() {
    io.grpc.MethodDescriptor<SubscribeRequest, SubscribeResponse> getSubscribeMethod;
    if ((getSubscribeMethod = DalalStreamServiceGrpc.getSubscribeMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getSubscribeMethod = DalalStreamServiceGrpc.getSubscribeMethod) == null) {
          DalalStreamServiceGrpc.getSubscribeMethod = getSubscribeMethod = 
              io.grpc.MethodDescriptor.<SubscribeRequest, SubscribeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "Subscribe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscribeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscribeResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getSubscribeMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getUnsubscribeMethod()} instead. 
  public static final io.grpc.MethodDescriptor<UnsubscribeRequest,
          UnsubscribeResponse> METHOD_UNSUBSCRIBE = getUnsubscribeMethod();

  private static volatile io.grpc.MethodDescriptor<UnsubscribeRequest,
      UnsubscribeResponse> getUnsubscribeMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<UnsubscribeRequest,
      UnsubscribeResponse> getUnsubscribeMethod() {
    io.grpc.MethodDescriptor<UnsubscribeRequest, UnsubscribeResponse> getUnsubscribeMethod;
    if ((getUnsubscribeMethod = DalalStreamServiceGrpc.getUnsubscribeMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getUnsubscribeMethod = DalalStreamServiceGrpc.getUnsubscribeMethod) == null) {
          DalalStreamServiceGrpc.getUnsubscribeMethod = getUnsubscribeMethod = 
              io.grpc.MethodDescriptor.<UnsubscribeRequest, UnsubscribeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "Unsubscribe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  UnsubscribeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  UnsubscribeResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getUnsubscribeMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMarketDepthUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
      MarketDepthUpdate> METHOD_GET_MARKET_DEPTH_UPDATES = getGetMarketDepthUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
      MarketDepthUpdate> getGetMarketDepthUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      MarketDepthUpdate> getGetMarketDepthUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, MarketDepthUpdate> getGetMarketDepthUpdatesMethod;
    if ((getGetMarketDepthUpdatesMethod = DalalStreamServiceGrpc.getGetMarketDepthUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetMarketDepthUpdatesMethod = DalalStreamServiceGrpc.getGetMarketDepthUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetMarketDepthUpdatesMethod = getGetMarketDepthUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, MarketDepthUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetMarketDepthUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  MarketDepthUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMarketDepthUpdatesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMarketEventUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
          MarketEventUpdate> METHOD_GET_MARKET_EVENT_UPDATES = getGetMarketEventUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
      MarketEventUpdate> getGetMarketEventUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      MarketEventUpdate> getGetMarketEventUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, MarketEventUpdate> getGetMarketEventUpdatesMethod;
    if ((getGetMarketEventUpdatesMethod = DalalStreamServiceGrpc.getGetMarketEventUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetMarketEventUpdatesMethod = DalalStreamServiceGrpc.getGetMarketEventUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetMarketEventUpdatesMethod = getGetMarketEventUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, MarketEventUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetMarketEventUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  MarketEventUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMarketEventUpdatesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMyOrderUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
      MyOrderUpdate> METHOD_GET_MY_ORDER_UPDATES = getGetMyOrderUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
      MyOrderUpdate> getGetMyOrderUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      MyOrderUpdate> getGetMyOrderUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, MyOrderUpdate> getGetMyOrderUpdatesMethod;
    if ((getGetMyOrderUpdatesMethod = DalalStreamServiceGrpc.getGetMyOrderUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetMyOrderUpdatesMethod = DalalStreamServiceGrpc.getGetMyOrderUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetMyOrderUpdatesMethod = getGetMyOrderUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, MyOrderUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetMyOrderUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  MyOrderUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMyOrderUpdatesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetNotificationUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
          NotificationUpdate> METHOD_GET_NOTIFICATION_UPDATES = getGetNotificationUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
      NotificationUpdate> getGetNotificationUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      NotificationUpdate> getGetNotificationUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, NotificationUpdate> getGetNotificationUpdatesMethod;
    if ((getGetNotificationUpdatesMethod = DalalStreamServiceGrpc.getGetNotificationUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetNotificationUpdatesMethod = DalalStreamServiceGrpc.getGetNotificationUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetNotificationUpdatesMethod = getGetNotificationUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, NotificationUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetNotificationUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  NotificationUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetNotificationUpdatesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetStockExchangeUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
      StockExchangeUpdate> METHOD_GET_STOCK_EXCHANGE_UPDATES = getGetStockExchangeUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
      StockExchangeUpdate> getGetStockExchangeUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      StockExchangeUpdate> getGetStockExchangeUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, StockExchangeUpdate> getGetStockExchangeUpdatesMethod;
    if ((getGetStockExchangeUpdatesMethod = DalalStreamServiceGrpc.getGetStockExchangeUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetStockExchangeUpdatesMethod = DalalStreamServiceGrpc.getGetStockExchangeUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetStockExchangeUpdatesMethod = getGetStockExchangeUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, StockExchangeUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetStockExchangeUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  StockExchangeUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetStockExchangeUpdatesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetStockPricesUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
          StockPricesUpdate> METHOD_GET_STOCK_PRICES_UPDATES = getGetStockPricesUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
      StockPricesUpdate> getGetStockPricesUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      StockPricesUpdate> getGetStockPricesUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, StockPricesUpdate> getGetStockPricesUpdatesMethod;
    if ((getGetStockPricesUpdatesMethod = DalalStreamServiceGrpc.getGetStockPricesUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetStockPricesUpdatesMethod = DalalStreamServiceGrpc.getGetStockPricesUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetStockPricesUpdatesMethod = getGetStockPricesUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, StockPricesUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetStockPricesUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  StockPricesUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetStockPricesUpdatesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetTransactionUpdatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<SubscriptionId,
      TransactionUpdate> METHOD_GET_TRANSACTION_UPDATES = getGetTransactionUpdatesMethod();

  private static volatile io.grpc.MethodDescriptor<SubscriptionId,
          TransactionUpdate> getGetTransactionUpdatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<SubscriptionId,
      TransactionUpdate> getGetTransactionUpdatesMethod() {
    io.grpc.MethodDescriptor<SubscriptionId, TransactionUpdate> getGetTransactionUpdatesMethod;
    if ((getGetTransactionUpdatesMethod = DalalStreamServiceGrpc.getGetTransactionUpdatesMethod) == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        if ((getGetTransactionUpdatesMethod = DalalStreamServiceGrpc.getGetTransactionUpdatesMethod) == null) {
          DalalStreamServiceGrpc.getGetTransactionUpdatesMethod = getGetTransactionUpdatesMethod = 
              io.grpc.MethodDescriptor.<SubscriptionId, TransactionUpdate>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proto.DalalStreamService", "GetTransactionUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  SubscriptionId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  TransactionUpdate.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetTransactionUpdatesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DalalStreamServiceStub newStub(io.grpc.Channel channel) {
    return new DalalStreamServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DalalStreamServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new DalalStreamServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DalalStreamServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new DalalStreamServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class DalalStreamServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Subscription
     * </pre>
     */
    public void subscribe(SubscribeRequest request,
        io.grpc.stub.StreamObserver<SubscribeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSubscribeMethod(), responseObserver);
    }

    /**
     */
    public void unsubscribe(UnsubscribeRequest request,
        io.grpc.stub.StreamObserver<UnsubscribeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUnsubscribeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Datastreams
     * </pre>
     */
    public void getMarketDepthUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<MarketDepthUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMarketDepthUpdatesMethod(), responseObserver);
    }

    /**
     */
    public void getMarketEventUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<MarketEventUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMarketEventUpdatesMethod(), responseObserver);
    }

    /**
     */
    public void getMyOrderUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<MyOrderUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMyOrderUpdatesMethod(), responseObserver);
    }

    /**
     */
    public void getNotificationUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<NotificationUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNotificationUpdatesMethod(), responseObserver);
    }

    /**
     */
    public void getStockExchangeUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<StockExchangeUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetStockExchangeUpdatesMethod(), responseObserver);
    }

    /**
     */
    public void getStockPricesUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<StockPricesUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetStockPricesUpdatesMethod(), responseObserver);
    }

    /**
     */
    public void getTransactionUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<TransactionUpdate> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTransactionUpdatesMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSubscribeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                SubscribeRequest,
                SubscribeResponse>(
                  this, METHODID_SUBSCRIBE)))
          .addMethod(
            getUnsubscribeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                UnsubscribeRequest,
                UnsubscribeResponse>(
                  this, METHODID_UNSUBSCRIBE)))
          .addMethod(
            getGetMarketDepthUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                MarketDepthUpdate>(
                  this, METHODID_GET_MARKET_DEPTH_UPDATES)))
          .addMethod(
            getGetMarketEventUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                MarketEventUpdate>(
                  this, METHODID_GET_MARKET_EVENT_UPDATES)))
          .addMethod(
            getGetMyOrderUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                MyOrderUpdate>(
                  this, METHODID_GET_MY_ORDER_UPDATES)))
          .addMethod(
            getGetNotificationUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                NotificationUpdate>(
                  this, METHODID_GET_NOTIFICATION_UPDATES)))
          .addMethod(
            getGetStockExchangeUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                StockExchangeUpdate>(
                  this, METHODID_GET_STOCK_EXCHANGE_UPDATES)))
          .addMethod(
            getGetStockPricesUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                StockPricesUpdate>(
                  this, METHODID_GET_STOCK_PRICES_UPDATES)))
          .addMethod(
            getGetTransactionUpdatesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                SubscriptionId,
                TransactionUpdate>(
                  this, METHODID_GET_TRANSACTION_UPDATES)))
          .build();
    }
  }

  /**
   */
  public static final class DalalStreamServiceStub extends io.grpc.stub.AbstractStub<DalalStreamServiceStub> {
    private DalalStreamServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DalalStreamServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected DalalStreamServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DalalStreamServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Subscription
     * </pre>
     */
    public void subscribe(SubscribeRequest request,
        io.grpc.stub.StreamObserver<SubscribeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSubscribeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void unsubscribe(UnsubscribeRequest request,
        io.grpc.stub.StreamObserver<UnsubscribeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUnsubscribeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Datastreams
     * </pre>
     */
    public void getMarketDepthUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<MarketDepthUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetMarketDepthUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMarketEventUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<MarketEventUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetMarketEventUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMyOrderUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<MyOrderUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetMyOrderUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNotificationUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<NotificationUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetNotificationUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getStockExchangeUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<StockExchangeUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetStockExchangeUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getStockPricesUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<StockPricesUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetStockPricesUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTransactionUpdates(SubscriptionId request,
        io.grpc.stub.StreamObserver<TransactionUpdate> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetTransactionUpdatesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DalalStreamServiceBlockingStub extends io.grpc.stub.AbstractStub<DalalStreamServiceBlockingStub> {
    private DalalStreamServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DalalStreamServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected DalalStreamServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DalalStreamServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Subscription
     * </pre>
     */
    public SubscribeResponse subscribe(SubscribeRequest request) {
      return blockingUnaryCall(
          getChannel(), getSubscribeMethod(), getCallOptions(), request);
    }

    /**
     */
    public UnsubscribeResponse unsubscribe(UnsubscribeRequest request) {
      return blockingUnaryCall(
          getChannel(), getUnsubscribeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Datastreams
     * </pre>
     */
    public java.util.Iterator<MarketDepthUpdate> getMarketDepthUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetMarketDepthUpdatesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<MarketEventUpdate> getMarketEventUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetMarketEventUpdatesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<MyOrderUpdate> getMyOrderUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetMyOrderUpdatesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<NotificationUpdate> getNotificationUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetNotificationUpdatesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<StockExchangeUpdate> getStockExchangeUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetStockExchangeUpdatesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<StockPricesUpdate> getStockPricesUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetStockPricesUpdatesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<TransactionUpdate> getTransactionUpdates(
        SubscriptionId request) {
      return blockingServerStreamingCall(
          getChannel(), getGetTransactionUpdatesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DalalStreamServiceFutureStub extends io.grpc.stub.AbstractStub<DalalStreamServiceFutureStub> {
    private DalalStreamServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DalalStreamServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected DalalStreamServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DalalStreamServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Subscription
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<SubscribeResponse> subscribe(
        SubscribeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSubscribeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<UnsubscribeResponse> unsubscribe(
        UnsubscribeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUnsubscribeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SUBSCRIBE = 0;
  private static final int METHODID_UNSUBSCRIBE = 1;
  private static final int METHODID_GET_MARKET_DEPTH_UPDATES = 2;
  private static final int METHODID_GET_MARKET_EVENT_UPDATES = 3;
  private static final int METHODID_GET_MY_ORDER_UPDATES = 4;
  private static final int METHODID_GET_NOTIFICATION_UPDATES = 5;
  private static final int METHODID_GET_STOCK_EXCHANGE_UPDATES = 6;
  private static final int METHODID_GET_STOCK_PRICES_UPDATES = 7;
  private static final int METHODID_GET_TRANSACTION_UPDATES = 8;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DalalStreamServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DalalStreamServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SUBSCRIBE:
          serviceImpl.subscribe((SubscribeRequest) request,
              (io.grpc.stub.StreamObserver<SubscribeResponse>) responseObserver);
          break;
        case METHODID_UNSUBSCRIBE:
          serviceImpl.unsubscribe((UnsubscribeRequest) request,
              (io.grpc.stub.StreamObserver<UnsubscribeResponse>) responseObserver);
          break;
        case METHODID_GET_MARKET_DEPTH_UPDATES:
          serviceImpl.getMarketDepthUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<MarketDepthUpdate>) responseObserver);
          break;
        case METHODID_GET_MARKET_EVENT_UPDATES:
          serviceImpl.getMarketEventUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<MarketEventUpdate>) responseObserver);
          break;
        case METHODID_GET_MY_ORDER_UPDATES:
          serviceImpl.getMyOrderUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<MyOrderUpdate>) responseObserver);
          break;
        case METHODID_GET_NOTIFICATION_UPDATES:
          serviceImpl.getNotificationUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<NotificationUpdate>) responseObserver);
          break;
        case METHODID_GET_STOCK_EXCHANGE_UPDATES:
          serviceImpl.getStockExchangeUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<StockExchangeUpdate>) responseObserver);
          break;
        case METHODID_GET_STOCK_PRICES_UPDATES:
          serviceImpl.getStockPricesUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<StockPricesUpdate>) responseObserver);
          break;
        case METHODID_GET_TRANSACTION_UPDATES:
          serviceImpl.getTransactionUpdates((SubscriptionId) request,
              (io.grpc.stub.StreamObserver<TransactionUpdate>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DalalStreamServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getSubscribeMethod())
              .addMethod(getUnsubscribeMethod())
              .addMethod(getGetMarketDepthUpdatesMethod())
              .addMethod(getGetMarketEventUpdatesMethod())
              .addMethod(getGetMyOrderUpdatesMethod())
              .addMethod(getGetNotificationUpdatesMethod())
              .addMethod(getGetStockExchangeUpdatesMethod())
              .addMethod(getGetStockPricesUpdatesMethod())
              .addMethod(getGetTransactionUpdatesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
