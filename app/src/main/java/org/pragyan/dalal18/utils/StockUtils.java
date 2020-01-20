package org.pragyan.dalal18.utils;

import org.pragyan.dalal18.data.GlobalStockDetails;
import org.pragyan.dalal18.data.StockDetails;

import java.util.List;

import dalalstreet.api.models.OrderType;

public class StockUtils {

    public static List<StockIdCompanyName> stockIdCompanyNameList;

    public static int getStockIdFromCompanyName(String incomingCompanyName) {

        for (StockIdCompanyName stockIdCompanyName : stockIdCompanyNameList) {
            if (stockIdCompanyName.getCompanyName().equalsIgnoreCase(incomingCompanyName))
                return stockIdCompanyName.getStockId();
        }
        return -1;
    }

    public static String getCompanyNameFromStockId(int id) {

        for (StockIdCompanyName stockIdCompanyName : stockIdCompanyNameList) {
            if (stockIdCompanyName.getStockId() == id)
                return stockIdCompanyName.getCompanyName();
        }
        return "";
    }

    public static String getCompanyNameFromShortName(String shortName) {

        for (StockIdCompanyName stockIdCompanyName : stockIdCompanyNameList) {
            if (stockIdCompanyName.getShortName().equals(shortName))
                return stockIdCompanyName.getCompanyName();
        }
        return "";
    }

    public static String[] getCompanyNamesArray() {
        String[] companyNames = new String[stockIdCompanyNameList.size()];
        for (int i = 0; i < stockIdCompanyNameList.size(); ++i) {
            companyNames[i] = stockIdCompanyNameList.get(i).getCompanyName();
        }
        return companyNames;
    }

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

    public static String getShortNameForStockId(int stockId) {
        for (StockIdCompanyName currentDetails : stockIdCompanyNameList) {
            if (currentDetails.getStockId() == stockId)
                return currentDetails.getShortName();
        }
        return "";
    }

    public static class StockIdCompanyName {

        private int stockId;
        private String companyName, shortName;

        public StockIdCompanyName(int stockId, String companyName, String shortName) {
            this.stockId = stockId;
            this.companyName = companyName;
            this.shortName = shortName;
        }

        int getStockId() {
            return stockId;
        }

        String getCompanyName() {
            return companyName;
        }

        String getShortName() {
            return shortName;
        }
    }

    public static long getQuantityOwnedFromCompanyName(List<StockDetails> list, String companyName) {
        int stockId = getStockIdFromCompanyName(companyName);

        for (StockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getQuantity();
        }
        return 0;
    }

    public static long getQuantityOwnedFromStockId(List<StockDetails> list, int stockId) {

        for (StockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getQuantity();
        }
        return 0;
    }

    public static String getDescriptionFromCompanyName(List<GlobalStockDetails> list, String companyName) {
        int stockId = getStockIdFromCompanyName(companyName);

        for (GlobalStockDetails globalStockDetails : list) {
            if (globalStockDetails.getStockId() == stockId)
                return globalStockDetails.getDescription();
        }

        return "";
    }

    public static String getImageUrlFromCompanyName(List<GlobalStockDetails> list, String companyName) {
        int stockId = getStockIdFromCompanyName(companyName);

        for (GlobalStockDetails globalStockDetails : list) {
            if (globalStockDetails.getStockId() == stockId)
                return globalStockDetails.getImagePath();
        }

        return "";
    }

    public static long getPriceFromStockId(List<GlobalStockDetails> list, int stockId) {

        for (GlobalStockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getPrice();
        }
        return 0;
    }

    public static long getPreviousDayCloseFromStockId(List<GlobalStockDetails> list, int stockId) {

        for (GlobalStockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getPreviousDayClose();
        }
        return 0;
    }

    /**
     * To get the index of element by company name.
     * Used in spinner in the trade fragment to manually selected an item on it.
     */
    public static int getIndexByCompanyName(String company) {
        int i = 0;
        // simple while loop breaking when company name is matched.
        while (i < StockUtils.getCompanyNamesArray().length) {
            if (company.equals(StockUtils.getCompanyNamesArray()[i]))
                break;
            i++;
        }
        return i;
    }

}