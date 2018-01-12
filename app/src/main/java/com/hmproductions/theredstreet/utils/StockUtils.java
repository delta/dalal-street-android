package com.hmproductions.theredstreet.utils;

import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

import dalalstreet.api.models.OrderType;

public class StockUtils {

    public static String[] companyNamesArray = new String[30];
    private static List<StockIdCompanyName> stockIdCompanyNameList;

    public static void createCompanyArrayFromGlobalStockDetails() {
        stockIdCompanyNameList = new ArrayList<>();

        for (int i=0 ; i< MainActivity.globalStockDetails.size() ; ++i) {
            if (MainActivity.globalStockDetails.get(i) != null) {
                GlobalStockDetails currentStockDetails = MainActivity.globalStockDetails.get(i);
                companyNamesArray[i] = currentStockDetails.getFullName();

                stockIdCompanyNameList.add(new StockIdCompanyName(currentStockDetails.getStockId(), currentStockDetails.getFullName()));
            }
        }
    }

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

    public static OrderType getOrderTypeFromName(String orderType) {

        switch (orderType) {
            
            case  "Limit Order"           : return  OrderType.LIMIT;
            case  "Market Order"          : return  OrderType.MARKET;
            case  "Stoploss Order"        : return  OrderType.STOPLOSS;
            case  "Stoploss Active Order" : return  OrderType.STOPLOSSACTIVE;
            default: return OrderType.UNRECOGNIZED;
        }
    }

    private static class StockIdCompanyName {

        private int stockId;
        private String companyName;

        StockIdCompanyName(int stockId, String companyName) {
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
}