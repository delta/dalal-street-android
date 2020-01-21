package org.pragyan.dalal18.utils;

public class Constants {

    // TODO (Release): Change server IP and Port

    public static final String HOST = "10.2.66.196";
    public static final int PORT = 8000;

    public static final int EVENT_START_TIME = 20;
    public static final int EVENT_END_TIME = 24;

    // TODO (Release): Change company image base URL and how is built
    public static final String COMPANY_IMAGES_BASE_URL = "https://dalal.pragyan.org/public/src/images/companies/";

    public static final int NUMBER_OF_COMPANIES = 30;

    public static final String REFRESH_NEWS_ACTION = "refresh-news-action";
    public static final String REFRESH_PRICE_TICKER_ACTION = "refresh-price-ticker-action";
    public static final String REFRESH_STOCK_PRICES_ACTION = "refresh-stock-prices-action";
    public static final String REFRESH_STOCKS_EXCHANGE_ACTION = "refresh-stock-exchange-action";
    public static final String REFRESH_OWNED_STOCKS_ACTION = "refresh-owned-stocks-action";
    public static final String REFRESH_RESERVED_ASSETS_ACTION = "refresh-reserved-stocks-action";
    public static final String REFRESH_MARKET_DEPTH = "refresh_market_depth";
    public static final String STOP_NOTIFICATION_ACTION = "stop-notification-action";
    public static final String REFRESH_MORTGAGE_UPDATE_ACTION = "refresh-mortgage-update-action";
    public static final String PRICE_FORMAT = "##,##,##,###";

    public static final String RUPEE_SYMBOL = "₹";

    public static double MORTGAGE_DEPOSIT_RATE = 0.8;
    public static double MORTGAGE_RETRIEVE_RATE = 0.9;
    public static double ORDER_FEE_RATE = 0.03;
    public static int ORDER_PRICE_WINDOW = 20;

    public static final String MARKET_OPEN_TEXT_KEY = "market-open-text-key";
    public static final String MARKET_CLOSED_TEXT_KEY = "market-closed-text-key";

    public static final String NOTIFICATION_SHARED_PREF = "notification_pref";
    public static final String NOTIFICATION_NEWS_SHARED_PREF = "notification_news_pref";

    public static final String USERNAME_KEY = "username-key";
    public static final String EMAIL_KEY = "email-key";
    public static final String SESSION_KEY = "session-key";
    public static final String MARKET_OPEN_KEY = "market-open-key";
    public static final String PASSWORD_KEY = "password-key";

    public static final String NEWS_HEAD_KEY = "news-head-key";
    public static final String NEWS_CONTENT_KEY = "news-content-key";
    public static final String NEWS_CREATED_AT_KEY = "news-created-at-key";
    public static final String NEWS_IMAGE_PATH_KEY = "news-image-path-key";
    public static final String HEAD_TRANSITION_KEY = "head-transition";
    public static final String CONTENT_TRANSITION_KEY = "content-transition";
    public static final String CREATED_AT_TRANSITION_KEY = "created-at-transition";

    public static final String LAST_NOTIFICATION_ID = "last_notification_id";
    public static final String LAST_TRANSACTION_ID = "last_transaction_id";
}

/*  Login response : Constants

    ORDER_PRICE_WINDOW"      -> 20
    LEADERBOARD_COUNT"       -> 10
    MY_BID_COUNT"            -> 10
    BUY_FROM_EXCHANGE_LIMIT" -> 20
    STARTING_CASH"           -> 200000
    BUY_LIMIT"               -> 30
    MORTGAGE_RETRIEVE_RATE"  -> 90
    ORDER_FEE_PERCENT"       -> 3
    SHORT_SELL_BORROW_LIMIT" -> 50
    BID_LIMIT"               -> 50
    MORTGAGE_DEPOSIT_RATE"   -> 80
    MARKET_EVENT_COUNT"      -> 10
    MY_ASK_COUNT"            -> 10
    GET_NOTIFICATION_COUNT"  -> 10
    GET_TRANSACTION_COUNT"   -> 10
    ASK_LIMIT"               -> 50
    MINIMUM_CASH_LIMIT"      -> 0
*/
