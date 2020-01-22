package org.pragyan.dalal18.utils;

import dalalstreet.api.models.OrderType;

public class OrderTypeUtils {

    public static OrderType getOrderTypeFromName(String orderType) {

        switch (orderType) {

            case "Limit Order":
                return OrderType.LIMIT;
            case "Market Order":
                return OrderType.MARKET;
            case "Stoploss Order":
                return OrderType.STOPLOSS;
            case "Stoploss Active Order":
                return OrderType.STOPLOSSACTIVE;
            default:
                return OrderType.UNRECOGNIZED;
        }
    }

    public static String getOrderTypeFromType(OrderType orderType) {
        if (orderType == OrderType.LIMIT) {
            return "Limit Order";
        } else if (orderType == OrderType.MARKET) {
            return "Market Order";
        } else if (orderType == OrderType.STOPLOSS) {
            return "Stoploss Order";
        } else {
            return "Unrecognized order";
        }
    }
}