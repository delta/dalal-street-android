package org.pragyan.dalal18.utils;

public class Constants {

    /* Test host - 139.59.47.250
     *  Test port - 443          */

    public static final String HOST = "192.168.1.106";
    public static final int PORT = 8000;

    public static final String COMPANY_IMAGES_BASE_URL = "https://dalal.pragyan.org/public/src/images/companies/";

    public static final int NUMBER_OF_COMPANIES = 30;

    public static final int LOGIN_LOADER_ID = 101;

    public static final int STOCK_HISTORY_LOADER_ID = 201;
    public static final int TRADE_LOADER_ID = 301;
    public static final int ORDERS_LOADER_ID = 401;
    public static final int COMPANY_PROFILE_LOADER_ID = 601;
    public static final int NEWS_LOADER_ID = 701;
    public static final int LEADER_BOARD_LOADER_ID = 801;
    public static final int TRANSACTION_LOADER_ID = 901;
    public static final int NOTIFICATION_LOADER_ID = 1001;

    public static final String REFRESH_NEWS_ACTION = "refresh-news-action";
    public static final String REFRESH_PRICE_TICKER_ACTION = "refresh-price-ticker-action";
    public static final String REFRESH_STOCK_PRICES_ACTION = "refresh-stock-prices-action";
    public static final String REFRESH_STOCKS_EXCHANGE_ACTION = "refresh-stock-exchange-action";
    public static final String REFRESH_OWNED_STOCKS_ACTION = "refresh-owned-stocks-action";
    public static final String REFRESH_MARKET_DEPTH= "refresh_market_depth";
    public static final String REFRESH_WORTH_TEXTVIEW_ACTION = "refresh-cash-worth-textview";
    public static final String REFRESH_DIVIDEND_ACTION = "refresh-dividend-worth-textview";
    public static final String UPDATE_WORTH_VIA_STREAM_ACTION = "refresh-worth-via-stream-action";
    public static final String STOP_NOTIFICATION_ACTION = "stop-notification-action";

    public static final String RUPEE_SYMBOL = "₹";

    public static double MORTGAGE_DEPOSIT_RATE = 0.8;
    public static double MORTGAGE_RETRIEVE_RATE = 0.9;
    public static final String MARKET_OPEN_TEXT_KEY = "market-open-text-key";
    public static final String MARKET_CLOSED_TEXT_KEY = "market-closed-text-key";

    public static final String NOTIFICATION_SHARED_PREF = "notification_pref";
    public static final String NOTIFICATION_NEWS_SHARED_PREF = "notification_news_pref";
}

/*  Login response : Constants

    SHORT_SELL_BORROW_LIMIT value : 50
    BID_LIMIT value : 50
    MORTGAGE_DEPOSIT_RATE value : 80
    GET_TRANSACTION_COUNT value : 10
    BUY_FROM_EXCHANGE_LIMIT value : 20
    MINIMUM_CASH_LIMIT value : 0
    STARTING_CASH value : 200000
    MY_ASK_COUNT value : 10
    MY_BID_COUNT value : 10
    LEADERBOARD_COUNT value : 10
    ASK_LIMIT value : 50
    BUY_LIMIT value : 30
    MORTGAGE_RETRIEVE_RATE value : 90
    MARKET_EVENT_COUNT value : 10
    GET_NOTIFICATION_COUNT value : 10
*/
