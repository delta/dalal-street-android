package dalalstreet.api;

import dalalstreet.api.actions.BuyStocksFromExchangeRequest;
import dalalstreet.api.actions.BuyStocksFromExchangeResponse;
import dalalstreet.api.actions.CancelOrderRequest;
import dalalstreet.api.actions.CancelOrderResponse;
import dalalstreet.api.actions.GetCompanyProfileRequest;
import dalalstreet.api.actions.GetCompanyProfileResponse;
import dalalstreet.api.actions.GetLeaderboardRequest;
import dalalstreet.api.actions.GetLeaderboardResponse;
import dalalstreet.api.actions.GetMarketEventsRequest;
import dalalstreet.api.actions.GetMarketEventsResponse;
import dalalstreet.api.actions.GetMortgageDetailsRequest;
import dalalstreet.api.actions.GetMortgageDetailsResponse;
import dalalstreet.api.actions.GetMyClosedAsksRequest;
import dalalstreet.api.actions.GetMyClosedAsksResponse;
import dalalstreet.api.actions.GetMyClosedBidsRequest;
import dalalstreet.api.actions.GetMyClosedBidsResponse;
import dalalstreet.api.actions.GetMyOpenOrdersRequest;
import dalalstreet.api.actions.GetMyOpenOrdersResponse;
import dalalstreet.api.actions.GetNotificationsRequest;
import dalalstreet.api.actions.GetNotificationsResponse;
import dalalstreet.api.actions.GetTransactionsRequest;
import dalalstreet.api.actions.GetTransactionsResponse;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import dalalstreet.api.actions.LogoutRequest;
import dalalstreet.api.actions.LogoutResponse;
import dalalstreet.api.actions.MortgageStocksRequest;
import dalalstreet.api.actions.MortgageStocksResponse;
import dalalstreet.api.actions.PlaceOrderRequest;
import dalalstreet.api.actions.PlaceOrderResponse;
import dalalstreet.api.actions.RetrieveMortgageStocksRequest;
import dalalstreet.api.actions.RetrieveMortgageStocksResponse;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.8.0)",
    comments = "Source: DalalMessage.proto")
public final class DalalActionServiceGrpc {

  private DalalActionServiceGrpc() {}

  public static final String SERVICE_NAME = "dalalstreet.api.DalalActionService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getBuyStocksFromExchangeMethod()} instead. 
  public static final io.grpc.MethodDescriptor<BuyStocksFromExchangeRequest,
          BuyStocksFromExchangeResponse> METHOD_BUY_STOCKS_FROM_EXCHANGE = getBuyStocksFromExchangeMethod();

  private static volatile io.grpc.MethodDescriptor<BuyStocksFromExchangeRequest,
      BuyStocksFromExchangeResponse> getBuyStocksFromExchangeMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<BuyStocksFromExchangeRequest,
      BuyStocksFromExchangeResponse> getBuyStocksFromExchangeMethod() {
    io.grpc.MethodDescriptor<BuyStocksFromExchangeRequest, BuyStocksFromExchangeResponse> getBuyStocksFromExchangeMethod;
    if ((getBuyStocksFromExchangeMethod = DalalActionServiceGrpc.getBuyStocksFromExchangeMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getBuyStocksFromExchangeMethod = DalalActionServiceGrpc.getBuyStocksFromExchangeMethod) == null) {
          DalalActionServiceGrpc.getBuyStocksFromExchangeMethod = getBuyStocksFromExchangeMethod = 
              io.grpc.MethodDescriptor.<BuyStocksFromExchangeRequest, BuyStocksFromExchangeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "BuyStocksFromExchange"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  BuyStocksFromExchangeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  BuyStocksFromExchangeResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getBuyStocksFromExchangeMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getPlaceOrderMethod()} instead. 
  public static final io.grpc.MethodDescriptor<PlaceOrderRequest,
          PlaceOrderResponse> METHOD_PLACE_ORDER = getPlaceOrderMethod();

  private static volatile io.grpc.MethodDescriptor<PlaceOrderRequest,
      PlaceOrderResponse> getPlaceOrderMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<PlaceOrderRequest,
      PlaceOrderResponse> getPlaceOrderMethod() {
    io.grpc.MethodDescriptor<PlaceOrderRequest, PlaceOrderResponse> getPlaceOrderMethod;
    if ((getPlaceOrderMethod = DalalActionServiceGrpc.getPlaceOrderMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getPlaceOrderMethod = DalalActionServiceGrpc.getPlaceOrderMethod) == null) {
          DalalActionServiceGrpc.getPlaceOrderMethod = getPlaceOrderMethod = 
              io.grpc.MethodDescriptor.<PlaceOrderRequest, PlaceOrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "PlaceOrder"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  PlaceOrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  PlaceOrderResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getPlaceOrderMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getCancelOrderMethod()} instead. 
  public static final io.grpc.MethodDescriptor<CancelOrderRequest,
          CancelOrderResponse> METHOD_CANCEL_ORDER = getCancelOrderMethod();

  private static volatile io.grpc.MethodDescriptor<CancelOrderRequest,
      CancelOrderResponse> getCancelOrderMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<CancelOrderRequest,
      CancelOrderResponse> getCancelOrderMethod() {
    io.grpc.MethodDescriptor<CancelOrderRequest, CancelOrderResponse> getCancelOrderMethod;
    if ((getCancelOrderMethod = DalalActionServiceGrpc.getCancelOrderMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getCancelOrderMethod = DalalActionServiceGrpc.getCancelOrderMethod) == null) {
          DalalActionServiceGrpc.getCancelOrderMethod = getCancelOrderMethod = 
              io.grpc.MethodDescriptor.<CancelOrderRequest, CancelOrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "CancelOrder"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  CancelOrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  CancelOrderResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getCancelOrderMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getMortgageStocksMethod()} instead. 
  public static final io.grpc.MethodDescriptor<MortgageStocksRequest,
      MortgageStocksResponse> METHOD_MORTGAGE_STOCKS = getMortgageStocksMethod();

  private static volatile io.grpc.MethodDescriptor<MortgageStocksRequest,
      MortgageStocksResponse> getMortgageStocksMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<MortgageStocksRequest,
      MortgageStocksResponse> getMortgageStocksMethod() {
    io.grpc.MethodDescriptor<MortgageStocksRequest, MortgageStocksResponse> getMortgageStocksMethod;
    if ((getMortgageStocksMethod = DalalActionServiceGrpc.getMortgageStocksMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getMortgageStocksMethod = DalalActionServiceGrpc.getMortgageStocksMethod) == null) {
          DalalActionServiceGrpc.getMortgageStocksMethod = getMortgageStocksMethod = 
              io.grpc.MethodDescriptor.<MortgageStocksRequest, MortgageStocksResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "MortgageStocks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  MortgageStocksRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  MortgageStocksResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getMortgageStocksMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getRetrieveMortgageStocksMethod()} instead. 
  public static final io.grpc.MethodDescriptor<RetrieveMortgageStocksRequest,
          RetrieveMortgageStocksResponse> METHOD_RETRIEVE_MORTGAGE_STOCKS = getRetrieveMortgageStocksMethod();

  private static volatile io.grpc.MethodDescriptor<RetrieveMortgageStocksRequest,
      RetrieveMortgageStocksResponse> getRetrieveMortgageStocksMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<RetrieveMortgageStocksRequest,
      RetrieveMortgageStocksResponse> getRetrieveMortgageStocksMethod() {
    io.grpc.MethodDescriptor<RetrieveMortgageStocksRequest, RetrieveMortgageStocksResponse> getRetrieveMortgageStocksMethod;
    if ((getRetrieveMortgageStocksMethod = DalalActionServiceGrpc.getRetrieveMortgageStocksMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getRetrieveMortgageStocksMethod = DalalActionServiceGrpc.getRetrieveMortgageStocksMethod) == null) {
          DalalActionServiceGrpc.getRetrieveMortgageStocksMethod = getRetrieveMortgageStocksMethod = 
              io.grpc.MethodDescriptor.<RetrieveMortgageStocksRequest, RetrieveMortgageStocksResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "RetrieveMortgageStocks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  RetrieveMortgageStocksRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  RetrieveMortgageStocksResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getRetrieveMortgageStocksMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMyOpenOrdersMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetMyOpenOrdersRequest,
      GetMyOpenOrdersResponse> METHOD_GET_MY_OPEN_ORDERS = getGetMyOpenOrdersMethod();

  private static volatile io.grpc.MethodDescriptor<GetMyOpenOrdersRequest,
      GetMyOpenOrdersResponse> getGetMyOpenOrdersMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetMyOpenOrdersRequest,
      GetMyOpenOrdersResponse> getGetMyOpenOrdersMethod() {
    io.grpc.MethodDescriptor<GetMyOpenOrdersRequest, GetMyOpenOrdersResponse> getGetMyOpenOrdersMethod;
    if ((getGetMyOpenOrdersMethod = DalalActionServiceGrpc.getGetMyOpenOrdersMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetMyOpenOrdersMethod = DalalActionServiceGrpc.getGetMyOpenOrdersMethod) == null) {
          DalalActionServiceGrpc.getGetMyOpenOrdersMethod = getGetMyOpenOrdersMethod = 
              io.grpc.MethodDescriptor.<GetMyOpenOrdersRequest, GetMyOpenOrdersResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetMyOpenOrders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMyOpenOrdersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMyOpenOrdersResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMyOpenOrdersMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMyClosedAsksMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetMyClosedAsksRequest,
          GetMyClosedAsksResponse> METHOD_GET_MY_CLOSED_ASKS = getGetMyClosedAsksMethod();

  private static volatile io.grpc.MethodDescriptor<GetMyClosedAsksRequest,
      GetMyClosedAsksResponse> getGetMyClosedAsksMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetMyClosedAsksRequest,
      GetMyClosedAsksResponse> getGetMyClosedAsksMethod() {
    io.grpc.MethodDescriptor<GetMyClosedAsksRequest, GetMyClosedAsksResponse> getGetMyClosedAsksMethod;
    if ((getGetMyClosedAsksMethod = DalalActionServiceGrpc.getGetMyClosedAsksMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetMyClosedAsksMethod = DalalActionServiceGrpc.getGetMyClosedAsksMethod) == null) {
          DalalActionServiceGrpc.getGetMyClosedAsksMethod = getGetMyClosedAsksMethod = 
              io.grpc.MethodDescriptor.<GetMyClosedAsksRequest, GetMyClosedAsksResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetMyClosedAsks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMyClosedAsksRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMyClosedAsksResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMyClosedAsksMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMyClosedBidsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetMyClosedBidsRequest,
      GetMyClosedBidsResponse> METHOD_GET_MY_CLOSED_BIDS = getGetMyClosedBidsMethod();

  private static volatile io.grpc.MethodDescriptor<GetMyClosedBidsRequest,
      GetMyClosedBidsResponse> getGetMyClosedBidsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetMyClosedBidsRequest,
      GetMyClosedBidsResponse> getGetMyClosedBidsMethod() {
    io.grpc.MethodDescriptor<GetMyClosedBidsRequest, GetMyClosedBidsResponse> getGetMyClosedBidsMethod;
    if ((getGetMyClosedBidsMethod = DalalActionServiceGrpc.getGetMyClosedBidsMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetMyClosedBidsMethod = DalalActionServiceGrpc.getGetMyClosedBidsMethod) == null) {
          DalalActionServiceGrpc.getGetMyClosedBidsMethod = getGetMyClosedBidsMethod = 
              io.grpc.MethodDescriptor.<GetMyClosedBidsRequest, GetMyClosedBidsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetMyClosedBids"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMyClosedBidsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMyClosedBidsResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMyClosedBidsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetTransactionsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetTransactionsRequest,
          GetTransactionsResponse> METHOD_GET_TRANSACTIONS = getGetTransactionsMethod();

  private static volatile io.grpc.MethodDescriptor<GetTransactionsRequest,
      GetTransactionsResponse> getGetTransactionsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetTransactionsRequest,
      GetTransactionsResponse> getGetTransactionsMethod() {
    io.grpc.MethodDescriptor<GetTransactionsRequest, GetTransactionsResponse> getGetTransactionsMethod;
    if ((getGetTransactionsMethod = DalalActionServiceGrpc.getGetTransactionsMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetTransactionsMethod = DalalActionServiceGrpc.getGetTransactionsMethod) == null) {
          DalalActionServiceGrpc.getGetTransactionsMethod = getGetTransactionsMethod = 
              io.grpc.MethodDescriptor.<GetTransactionsRequest, GetTransactionsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetTransactionsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetTransactionsResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetTransactionsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMortgageDetailsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetMortgageDetailsRequest,
      GetMortgageDetailsResponse> METHOD_GET_MORTGAGE_DETAILS = getGetMortgageDetailsMethod();

  private static volatile io.grpc.MethodDescriptor<GetMortgageDetailsRequest,
      GetMortgageDetailsResponse> getGetMortgageDetailsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetMortgageDetailsRequest,
      GetMortgageDetailsResponse> getGetMortgageDetailsMethod() {
    io.grpc.MethodDescriptor<GetMortgageDetailsRequest, GetMortgageDetailsResponse> getGetMortgageDetailsMethod;
    if ((getGetMortgageDetailsMethod = DalalActionServiceGrpc.getGetMortgageDetailsMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetMortgageDetailsMethod = DalalActionServiceGrpc.getGetMortgageDetailsMethod) == null) {
          DalalActionServiceGrpc.getGetMortgageDetailsMethod = getGetMortgageDetailsMethod = 
              io.grpc.MethodDescriptor.<GetMortgageDetailsRequest, GetMortgageDetailsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetMortgageDetails"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMortgageDetailsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMortgageDetailsResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMortgageDetailsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetCompanyProfileMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetCompanyProfileRequest,
          GetCompanyProfileResponse> METHOD_GET_COMPANY_PROFILE = getGetCompanyProfileMethod();

  private static volatile io.grpc.MethodDescriptor<GetCompanyProfileRequest,
      GetCompanyProfileResponse> getGetCompanyProfileMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetCompanyProfileRequest,
      GetCompanyProfileResponse> getGetCompanyProfileMethod() {
    io.grpc.MethodDescriptor<GetCompanyProfileRequest, GetCompanyProfileResponse> getGetCompanyProfileMethod;
    if ((getGetCompanyProfileMethod = DalalActionServiceGrpc.getGetCompanyProfileMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetCompanyProfileMethod = DalalActionServiceGrpc.getGetCompanyProfileMethod) == null) {
          DalalActionServiceGrpc.getGetCompanyProfileMethod = getGetCompanyProfileMethod = 
              io.grpc.MethodDescriptor.<GetCompanyProfileRequest, GetCompanyProfileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetCompanyProfile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetCompanyProfileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetCompanyProfileResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetCompanyProfileMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetLeaderboardMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetLeaderboardRequest,
      GetLeaderboardResponse> METHOD_GET_LEADERBOARD = getGetLeaderboardMethod();

  private static volatile io.grpc.MethodDescriptor<GetLeaderboardRequest,
      GetLeaderboardResponse> getGetLeaderboardMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetLeaderboardRequest,
      GetLeaderboardResponse> getGetLeaderboardMethod() {
    io.grpc.MethodDescriptor<GetLeaderboardRequest, GetLeaderboardResponse> getGetLeaderboardMethod;
    if ((getGetLeaderboardMethod = DalalActionServiceGrpc.getGetLeaderboardMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetLeaderboardMethod = DalalActionServiceGrpc.getGetLeaderboardMethod) == null) {
          DalalActionServiceGrpc.getGetLeaderboardMethod = getGetLeaderboardMethod = 
              io.grpc.MethodDescriptor.<GetLeaderboardRequest, GetLeaderboardResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetLeaderboard"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetLeaderboardRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetLeaderboardResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetLeaderboardMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetMarketEventsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetMarketEventsRequest,
      GetMarketEventsResponse> METHOD_GET_MARKET_EVENTS = getGetMarketEventsMethod();

  private static volatile io.grpc.MethodDescriptor<GetMarketEventsRequest,
      GetMarketEventsResponse> getGetMarketEventsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetMarketEventsRequest,
      GetMarketEventsResponse> getGetMarketEventsMethod() {
    io.grpc.MethodDescriptor<GetMarketEventsRequest, GetMarketEventsResponse> getGetMarketEventsMethod;
    if ((getGetMarketEventsMethod = DalalActionServiceGrpc.getGetMarketEventsMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetMarketEventsMethod = DalalActionServiceGrpc.getGetMarketEventsMethod) == null) {
          DalalActionServiceGrpc.getGetMarketEventsMethod = getGetMarketEventsMethod = 
              io.grpc.MethodDescriptor.<GetMarketEventsRequest, GetMarketEventsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetMarketEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMarketEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetMarketEventsResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetMarketEventsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getGetNotificationsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<GetNotificationsRequest,
      GetNotificationsResponse> METHOD_GET_NOTIFICATIONS = getGetNotificationsMethod();

  private static volatile io.grpc.MethodDescriptor<GetNotificationsRequest,
          GetNotificationsResponse> getGetNotificationsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<GetNotificationsRequest,
      GetNotificationsResponse> getGetNotificationsMethod() {
    io.grpc.MethodDescriptor<GetNotificationsRequest, GetNotificationsResponse> getGetNotificationsMethod;
    if ((getGetNotificationsMethod = DalalActionServiceGrpc.getGetNotificationsMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getGetNotificationsMethod = DalalActionServiceGrpc.getGetNotificationsMethod) == null) {
          DalalActionServiceGrpc.getGetNotificationsMethod = getGetNotificationsMethod = 
              io.grpc.MethodDescriptor.<GetNotificationsRequest, GetNotificationsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "GetNotifications"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetNotificationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  GetNotificationsResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetNotificationsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getLoginMethod()} instead. 
  public static final io.grpc.MethodDescriptor<LoginRequest,
      LoginResponse> METHOD_LOGIN = getLoginMethod();

  private static volatile io.grpc.MethodDescriptor<LoginRequest,
      LoginResponse> getLoginMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<LoginRequest,
      LoginResponse> getLoginMethod() {
    io.grpc.MethodDescriptor<LoginRequest, LoginResponse> getLoginMethod;
    if ((getLoginMethod = DalalActionServiceGrpc.getLoginMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getLoginMethod = DalalActionServiceGrpc.getLoginMethod) == null) {
          DalalActionServiceGrpc.getLoginMethod = getLoginMethod = 
              io.grpc.MethodDescriptor.<LoginRequest, LoginResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "Login"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  LoginRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  LoginResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getLoginMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @Deprecated // Use {@link #getLogoutMethod()} instead. 
  public static final io.grpc.MethodDescriptor<LogoutRequest,
      LogoutResponse> METHOD_LOGOUT = getLogoutMethod();

  private static volatile io.grpc.MethodDescriptor<LogoutRequest,
      LogoutResponse> getLogoutMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<LogoutRequest,
      LogoutResponse> getLogoutMethod() {
    io.grpc.MethodDescriptor<LogoutRequest, LogoutResponse> getLogoutMethod;
    if ((getLogoutMethod = DalalActionServiceGrpc.getLogoutMethod) == null) {
      synchronized (DalalActionServiceGrpc.class) {
        if ((getLogoutMethod = DalalActionServiceGrpc.getLogoutMethod) == null) {
          DalalActionServiceGrpc.getLogoutMethod = getLogoutMethod = 
              io.grpc.MethodDescriptor.<LogoutRequest, LogoutResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "dalalstreet.api.DalalActionService", "Logout"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  LogoutRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  LogoutResponse.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getLogoutMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DalalActionServiceStub newStub(io.grpc.Channel channel) {
    return new DalalActionServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DalalActionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new DalalActionServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DalalActionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new DalalActionServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class DalalActionServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Stock trading related functions
     * </pre>
     */
    public void buyStocksFromExchange(BuyStocksFromExchangeRequest request,
        io.grpc.stub.StreamObserver<BuyStocksFromExchangeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getBuyStocksFromExchangeMethod(), responseObserver);
    }

    /**
     */
    public void placeOrder(PlaceOrderRequest request,
        io.grpc.stub.StreamObserver<PlaceOrderResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPlaceOrderMethod(), responseObserver);
    }

    /**
     */
    public void cancelOrder(CancelOrderRequest request,
        io.grpc.stub.StreamObserver<CancelOrderResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCancelOrderMethod(), responseObserver);
    }

    /**
     */
    public void mortgageStocks(MortgageStocksRequest request,
        io.grpc.stub.StreamObserver<MortgageStocksResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMortgageStocksMethod(), responseObserver);
    }

    /**
     */
    public void retrieveMortgageStocks(RetrieveMortgageStocksRequest request,
        io.grpc.stub.StreamObserver<RetrieveMortgageStocksResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRetrieveMortgageStocksMethod(), responseObserver);
    }

    /**
     * <pre>
     * Getting information about transactions, orders, mortgage
     * </pre>
     */
    public void getMyOpenOrders(GetMyOpenOrdersRequest request,
        io.grpc.stub.StreamObserver<GetMyOpenOrdersResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMyOpenOrdersMethod(), responseObserver);
    }

    /**
     */
    public void getMyClosedAsks(GetMyClosedAsksRequest request,
        io.grpc.stub.StreamObserver<GetMyClosedAsksResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMyClosedAsksMethod(), responseObserver);
    }

    /**
     */
    public void getMyClosedBids(GetMyClosedBidsRequest request,
        io.grpc.stub.StreamObserver<GetMyClosedBidsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMyClosedBidsMethod(), responseObserver);
    }

    /**
     */
    public void getTransactions(GetTransactionsRequest request,
        io.grpc.stub.StreamObserver<GetTransactionsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTransactionsMethod(), responseObserver);
    }

    /**
     */
    public void getMortgageDetails(GetMortgageDetailsRequest request,
        io.grpc.stub.StreamObserver<GetMortgageDetailsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMortgageDetailsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Getting general information
     * </pre>
     */
    public void getCompanyProfile(GetCompanyProfileRequest request,
        io.grpc.stub.StreamObserver<GetCompanyProfileResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCompanyProfileMethod(), responseObserver);
    }

    /**
     */
    public void getLeaderboard(GetLeaderboardRequest request,
        io.grpc.stub.StreamObserver<GetLeaderboardResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetLeaderboardMethod(), responseObserver);
    }

    /**
     */
    public void getMarketEvents(GetMarketEventsRequest request,
        io.grpc.stub.StreamObserver<GetMarketEventsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMarketEventsMethod(), responseObserver);
    }

    /**
     */
    public void getNotifications(GetNotificationsRequest request,
        io.grpc.stub.StreamObserver<GetNotificationsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNotificationsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Auth
     * </pre>
     */
    public void login(LoginRequest request,
        io.grpc.stub.StreamObserver<LoginResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getLoginMethod(), responseObserver);
    }

    /**
     */
    public void logout(LogoutRequest request,
        io.grpc.stub.StreamObserver<LogoutResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getLogoutMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getBuyStocksFromExchangeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                BuyStocksFromExchangeRequest,
                BuyStocksFromExchangeResponse>(
                  this, METHODID_BUY_STOCKS_FROM_EXCHANGE)))
          .addMethod(
            getPlaceOrderMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                PlaceOrderRequest,
                PlaceOrderResponse>(
                  this, METHODID_PLACE_ORDER)))
          .addMethod(
            getCancelOrderMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                CancelOrderRequest,
                CancelOrderResponse>(
                  this, METHODID_CANCEL_ORDER)))
          .addMethod(
            getMortgageStocksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                MortgageStocksRequest,
                MortgageStocksResponse>(
                  this, METHODID_MORTGAGE_STOCKS)))
          .addMethod(
            getRetrieveMortgageStocksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                RetrieveMortgageStocksRequest,
                RetrieveMortgageStocksResponse>(
                  this, METHODID_RETRIEVE_MORTGAGE_STOCKS)))
          .addMethod(
            getGetMyOpenOrdersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetMyOpenOrdersRequest,
                GetMyOpenOrdersResponse>(
                  this, METHODID_GET_MY_OPEN_ORDERS)))
          .addMethod(
            getGetMyClosedAsksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetMyClosedAsksRequest,
                GetMyClosedAsksResponse>(
                  this, METHODID_GET_MY_CLOSED_ASKS)))
          .addMethod(
            getGetMyClosedBidsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetMyClosedBidsRequest,
                GetMyClosedBidsResponse>(
                  this, METHODID_GET_MY_CLOSED_BIDS)))
          .addMethod(
            getGetTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetTransactionsRequest,
                GetTransactionsResponse>(
                  this, METHODID_GET_TRANSACTIONS)))
          .addMethod(
            getGetMortgageDetailsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetMortgageDetailsRequest,
                GetMortgageDetailsResponse>(
                  this, METHODID_GET_MORTGAGE_DETAILS)))
          .addMethod(
            getGetCompanyProfileMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetCompanyProfileRequest,
                GetCompanyProfileResponse>(
                  this, METHODID_GET_COMPANY_PROFILE)))
          .addMethod(
            getGetLeaderboardMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetLeaderboardRequest,
                GetLeaderboardResponse>(
                  this, METHODID_GET_LEADERBOARD)))
          .addMethod(
            getGetMarketEventsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetMarketEventsRequest,
                GetMarketEventsResponse>(
                  this, METHODID_GET_MARKET_EVENTS)))
          .addMethod(
            getGetNotificationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                GetNotificationsRequest,
                GetNotificationsResponse>(
                  this, METHODID_GET_NOTIFICATIONS)))
          .addMethod(
            getLoginMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                LoginRequest,
                LoginResponse>(
                  this, METHODID_LOGIN)))
          .addMethod(
            getLogoutMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                LogoutRequest,
                LogoutResponse>(
                  this, METHODID_LOGOUT)))
          .build();
    }
  }

  /**
   */
  public static final class DalalActionServiceStub extends io.grpc.stub.AbstractStub<DalalActionServiceStub> {
    private DalalActionServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DalalActionServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected DalalActionServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DalalActionServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Stock trading related functions
     * </pre>
     */
    public void buyStocksFromExchange(BuyStocksFromExchangeRequest request,
        io.grpc.stub.StreamObserver<BuyStocksFromExchangeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getBuyStocksFromExchangeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void placeOrder(PlaceOrderRequest request,
        io.grpc.stub.StreamObserver<PlaceOrderResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPlaceOrderMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void cancelOrder(CancelOrderRequest request,
        io.grpc.stub.StreamObserver<CancelOrderResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCancelOrderMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void mortgageStocks(MortgageStocksRequest request,
        io.grpc.stub.StreamObserver<MortgageStocksResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMortgageStocksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void retrieveMortgageStocks(RetrieveMortgageStocksRequest request,
        io.grpc.stub.StreamObserver<RetrieveMortgageStocksResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRetrieveMortgageStocksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Getting information about transactions, orders, mortgage
     * </pre>
     */
    public void getMyOpenOrders(GetMyOpenOrdersRequest request,
        io.grpc.stub.StreamObserver<GetMyOpenOrdersResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMyOpenOrdersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMyClosedAsks(GetMyClosedAsksRequest request,
        io.grpc.stub.StreamObserver<GetMyClosedAsksResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMyClosedAsksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMyClosedBids(GetMyClosedBidsRequest request,
        io.grpc.stub.StreamObserver<GetMyClosedBidsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMyClosedBidsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTransactions(GetTransactionsRequest request,
        io.grpc.stub.StreamObserver<GetTransactionsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetTransactionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMortgageDetails(GetMortgageDetailsRequest request,
        io.grpc.stub.StreamObserver<GetMortgageDetailsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMortgageDetailsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Getting general information
     * </pre>
     */
    public void getCompanyProfile(GetCompanyProfileRequest request,
        io.grpc.stub.StreamObserver<GetCompanyProfileResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCompanyProfileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getLeaderboard(GetLeaderboardRequest request,
        io.grpc.stub.StreamObserver<GetLeaderboardResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetLeaderboardMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMarketEvents(GetMarketEventsRequest request,
        io.grpc.stub.StreamObserver<GetMarketEventsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMarketEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNotifications(GetNotificationsRequest request,
        io.grpc.stub.StreamObserver<GetNotificationsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetNotificationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Auth
     * </pre>
     */
    public void login(LoginRequest request,
        io.grpc.stub.StreamObserver<LoginResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getLoginMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void logout(LogoutRequest request,
        io.grpc.stub.StreamObserver<LogoutResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getLogoutMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DalalActionServiceBlockingStub extends io.grpc.stub.AbstractStub<DalalActionServiceBlockingStub> {
    private DalalActionServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DalalActionServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected DalalActionServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DalalActionServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Stock trading related functions
     * </pre>
     */
    public BuyStocksFromExchangeResponse buyStocksFromExchange(BuyStocksFromExchangeRequest request) {
      return blockingUnaryCall(
          getChannel(), getBuyStocksFromExchangeMethod(), getCallOptions(), request);
    }

    /**
     */
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
      return blockingUnaryCall(
          getChannel(), getPlaceOrderMethod(), getCallOptions(), request);
    }

    /**
     */
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
      return blockingUnaryCall(
          getChannel(), getCancelOrderMethod(), getCallOptions(), request);
    }

    /**
     */
    public MortgageStocksResponse mortgageStocks(MortgageStocksRequest request) {
      return blockingUnaryCall(
          getChannel(), getMortgageStocksMethod(), getCallOptions(), request);
    }

    /**
     */
    public RetrieveMortgageStocksResponse retrieveMortgageStocks(RetrieveMortgageStocksRequest request) {
      return blockingUnaryCall(
          getChannel(), getRetrieveMortgageStocksMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Getting information about transactions, orders, mortgage
     * </pre>
     */
    public GetMyOpenOrdersResponse getMyOpenOrders(GetMyOpenOrdersRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMyOpenOrdersMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetMyClosedAsksResponse getMyClosedAsks(GetMyClosedAsksRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMyClosedAsksMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetMyClosedBidsResponse getMyClosedBids(GetMyClosedBidsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMyClosedBidsMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetTransactionsResponse getTransactions(GetTransactionsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetTransactionsMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetMortgageDetailsResponse getMortgageDetails(GetMortgageDetailsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMortgageDetailsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Getting general information
     * </pre>
     */
    public GetCompanyProfileResponse getCompanyProfile(GetCompanyProfileRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetCompanyProfileMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetLeaderboardResponse getLeaderboard(GetLeaderboardRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetLeaderboardMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetMarketEventsResponse getMarketEvents(GetMarketEventsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMarketEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public GetNotificationsResponse getNotifications(GetNotificationsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetNotificationsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Auth
     * </pre>
     */
    public LoginResponse login(LoginRequest request) {
      return blockingUnaryCall(
          getChannel(), getLoginMethod(), getCallOptions(), request);
    }

    /**
     */
    public LogoutResponse logout(LogoutRequest request) {
      return blockingUnaryCall(
          getChannel(), getLogoutMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DalalActionServiceFutureStub extends io.grpc.stub.AbstractStub<DalalActionServiceFutureStub> {
    private DalalActionServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DalalActionServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected DalalActionServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DalalActionServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Stock trading related functions
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<BuyStocksFromExchangeResponse> buyStocksFromExchange(
        BuyStocksFromExchangeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getBuyStocksFromExchangeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<PlaceOrderResponse> placeOrder(
        PlaceOrderRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPlaceOrderMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<CancelOrderResponse> cancelOrder(
        CancelOrderRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCancelOrderMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<MortgageStocksResponse> mortgageStocks(
        MortgageStocksRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMortgageStocksMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<RetrieveMortgageStocksResponse> retrieveMortgageStocks(
        RetrieveMortgageStocksRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRetrieveMortgageStocksMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Getting information about transactions, orders, mortgage
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<GetMyOpenOrdersResponse> getMyOpenOrders(
        GetMyOpenOrdersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMyOpenOrdersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetMyClosedAsksResponse> getMyClosedAsks(
        GetMyClosedAsksRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMyClosedAsksMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetMyClosedBidsResponse> getMyClosedBids(
        GetMyClosedBidsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMyClosedBidsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetTransactionsResponse> getTransactions(
        GetTransactionsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetTransactionsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetMortgageDetailsResponse> getMortgageDetails(
        GetMortgageDetailsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMortgageDetailsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Getting general information
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<GetCompanyProfileResponse> getCompanyProfile(
        GetCompanyProfileRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCompanyProfileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetLeaderboardResponse> getLeaderboard(
        GetLeaderboardRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetLeaderboardMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetMarketEventsResponse> getMarketEvents(
        GetMarketEventsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMarketEventsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<GetNotificationsResponse> getNotifications(
        GetNotificationsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetNotificationsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Auth
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<LoginResponse> login(
        LoginRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getLoginMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<LogoutResponse> logout(
        LogoutRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getLogoutMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_BUY_STOCKS_FROM_EXCHANGE = 0;
  private static final int METHODID_PLACE_ORDER = 1;
  private static final int METHODID_CANCEL_ORDER = 2;
  private static final int METHODID_MORTGAGE_STOCKS = 3;
  private static final int METHODID_RETRIEVE_MORTGAGE_STOCKS = 4;
  private static final int METHODID_GET_MY_OPEN_ORDERS = 5;
  private static final int METHODID_GET_MY_CLOSED_ASKS = 6;
  private static final int METHODID_GET_MY_CLOSED_BIDS = 7;
  private static final int METHODID_GET_TRANSACTIONS = 8;
  private static final int METHODID_GET_MORTGAGE_DETAILS = 9;
  private static final int METHODID_GET_COMPANY_PROFILE = 10;
  private static final int METHODID_GET_LEADERBOARD = 11;
  private static final int METHODID_GET_MARKET_EVENTS = 12;
  private static final int METHODID_GET_NOTIFICATIONS = 13;
  private static final int METHODID_LOGIN = 14;
  private static final int METHODID_LOGOUT = 15;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DalalActionServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DalalActionServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_BUY_STOCKS_FROM_EXCHANGE:
          serviceImpl.buyStocksFromExchange((BuyStocksFromExchangeRequest) request,
              (io.grpc.stub.StreamObserver<BuyStocksFromExchangeResponse>) responseObserver);
          break;
        case METHODID_PLACE_ORDER:
          serviceImpl.placeOrder((PlaceOrderRequest) request,
              (io.grpc.stub.StreamObserver<PlaceOrderResponse>) responseObserver);
          break;
        case METHODID_CANCEL_ORDER:
          serviceImpl.cancelOrder((CancelOrderRequest) request,
              (io.grpc.stub.StreamObserver<CancelOrderResponse>) responseObserver);
          break;
        case METHODID_MORTGAGE_STOCKS:
          serviceImpl.mortgageStocks((MortgageStocksRequest) request,
              (io.grpc.stub.StreamObserver<MortgageStocksResponse>) responseObserver);
          break;
        case METHODID_RETRIEVE_MORTGAGE_STOCKS:
          serviceImpl.retrieveMortgageStocks((RetrieveMortgageStocksRequest) request,
              (io.grpc.stub.StreamObserver<RetrieveMortgageStocksResponse>) responseObserver);
          break;
        case METHODID_GET_MY_OPEN_ORDERS:
          serviceImpl.getMyOpenOrders((GetMyOpenOrdersRequest) request,
              (io.grpc.stub.StreamObserver<GetMyOpenOrdersResponse>) responseObserver);
          break;
        case METHODID_GET_MY_CLOSED_ASKS:
          serviceImpl.getMyClosedAsks((GetMyClosedAsksRequest) request,
              (io.grpc.stub.StreamObserver<GetMyClosedAsksResponse>) responseObserver);
          break;
        case METHODID_GET_MY_CLOSED_BIDS:
          serviceImpl.getMyClosedBids((GetMyClosedBidsRequest) request,
              (io.grpc.stub.StreamObserver<GetMyClosedBidsResponse>) responseObserver);
          break;
        case METHODID_GET_TRANSACTIONS:
          serviceImpl.getTransactions((GetTransactionsRequest) request,
              (io.grpc.stub.StreamObserver<GetTransactionsResponse>) responseObserver);
          break;
        case METHODID_GET_MORTGAGE_DETAILS:
          serviceImpl.getMortgageDetails((GetMortgageDetailsRequest) request,
              (io.grpc.stub.StreamObserver<GetMortgageDetailsResponse>) responseObserver);
          break;
        case METHODID_GET_COMPANY_PROFILE:
          serviceImpl.getCompanyProfile((GetCompanyProfileRequest) request,
              (io.grpc.stub.StreamObserver<GetCompanyProfileResponse>) responseObserver);
          break;
        case METHODID_GET_LEADERBOARD:
          serviceImpl.getLeaderboard((GetLeaderboardRequest) request,
              (io.grpc.stub.StreamObserver<GetLeaderboardResponse>) responseObserver);
          break;
        case METHODID_GET_MARKET_EVENTS:
          serviceImpl.getMarketEvents((GetMarketEventsRequest) request,
              (io.grpc.stub.StreamObserver<GetMarketEventsResponse>) responseObserver);
          break;
        case METHODID_GET_NOTIFICATIONS:
          serviceImpl.getNotifications((GetNotificationsRequest) request,
              (io.grpc.stub.StreamObserver<GetNotificationsResponse>) responseObserver);
          break;
        case METHODID_LOGIN:
          serviceImpl.login((LoginRequest) request,
              (io.grpc.stub.StreamObserver<LoginResponse>) responseObserver);
          break;
        case METHODID_LOGOUT:
          serviceImpl.logout((LogoutRequest) request,
              (io.grpc.stub.StreamObserver<LogoutResponse>) responseObserver);
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
      synchronized (DalalActionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getBuyStocksFromExchangeMethod())
              .addMethod(getPlaceOrderMethod())
              .addMethod(getCancelOrderMethod())
              .addMethod(getMortgageStocksMethod())
              .addMethod(getRetrieveMortgageStocksMethod())
              .addMethod(getGetMyOpenOrdersMethod())
              .addMethod(getGetMyClosedAsksMethod())
              .addMethod(getGetMyClosedBidsMethod())
              .addMethod(getGetTransactionsMethod())
              .addMethod(getGetMortgageDetailsMethod())
              .addMethod(getGetCompanyProfileMethod())
              .addMethod(getGetLeaderboardMethod())
              .addMethod(getGetMarketEventsMethod())
              .addMethod(getGetNotificationsMethod())
              .addMethod(getLoginMethod())
              .addMethod(getLogoutMethod())
              .build();
        }
      }
    }
    return result;
  }
}
