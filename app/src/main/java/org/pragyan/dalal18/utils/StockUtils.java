package org.pragyan.dalal18.utils;

import org.pragyan.dalal18.data.GlobalStockDetails;
import org.pragyan.dalal18.data.StockDetails;
import org.pragyan.dalal18.ui.MainActivity;

import java.util.ArrayList;
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

    public static String[] getCompanyNamesArray() {
        String[] companyNames = new String[stockIdCompanyNameList.size()];
        for (int i=0 ; i<stockIdCompanyNameList.size() ; ++i) {
            companyNames[i] = stockIdCompanyNameList.get(i).getCompanyName();
        }
        return companyNames;
    }

    public static OrderType getOrderTypeFromName(String orderType) {

        switch (orderType) {
            
            case  "Limit Order"           : return  OrderType.LIMIT;
            case  "Market Order"          : return  OrderType.MARKET;
            case  "Stoploss Order"        : return  OrderType.STOPLOSS;
            case  "Stoploss Active Order" : return  OrderType.STOPLOSSACTIVE;
            default: return OrderType.UNRECOGNIZED;
        }
    }

    public static String getOrderTypeFromTypeId(int id) {
        switch (id) {
            case 0 : return "Limit Order";
            case 1 : return "Market Order";
            case 2 : return "Stoploss Order";
            default: return "Unrecognized order";
        }
    }

    public static String getShortNameForStockId(List<GlobalStockDetails> globalStockDetails, int stockId) {
        for (GlobalStockDetails currentDetails : globalStockDetails) {
            if (currentDetails.getStockId() == stockId)
                return currentDetails.getShortName();
        }
        return null;
    }

    public static class StockIdCompanyName {

        private int stockId;
        private String companyName;

        public StockIdCompanyName(int stockId, String companyName) {
            this.stockId = stockId;
            this.companyName = companyName;
        }

        int getStockId() {
            return stockId;
        }

        String getCompanyName() {
            return companyName;
        }
    }

    public static int getQuantityOwnedFromCompanyName(List<StockDetails> list, String companyName) {
        int stockId = getStockIdFromCompanyName(companyName);

        for (StockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getQuantity();
        }
        return 0;
    }

    public static int getPriceFromStockId(List<GlobalStockDetails> list, int stockId) {

        for (GlobalStockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getPrice();
        }
        return 0;
    }

    public static int getPreviousDayCloseFromStockId(List<GlobalStockDetails> list, int stockId) {

        for (GlobalStockDetails stockDetails : list) {
            if (stockDetails.getStockId() == stockId)
                return stockDetails.getPreviousDayClose();
        }
        return 0;
    }
}