package org.pragyan.dalal18.data

import androidx.lifecycle.ViewModel

class DalalViewModel : ViewModel() {

    lateinit var ownedStockDetails: MutableList<StockDetails>
    lateinit var globalStockDetails: MutableList<GlobalStockDetails>
    lateinit var reservedStockDetails: MutableList<StockDetails>

    private var stockIdCompanyNameList = mutableListOf<StockIdCompanyName>()

    var companyName: String? = null

    var reservedCash = 0L

    fun updateGlobalStock(position: Int, price: Long, quantityInMarket: Long, quantityInExchange: Long) {
        globalStockDetails[position].price = price
        globalStockDetails[position].quantityInMarket = quantityInMarket
        globalStockDetails[position].quantityInExchange = quantityInExchange
    }

    fun updateGlobalStockPrice(pos: Int, price: Long) {
        globalStockDetails[pos - 1].price = price
    }

    fun createCompanyArrayFromGlobalStockDetails() {

        for (currentStockDetails in globalStockDetails) {
            stockIdCompanyNameList.add(StockIdCompanyName(currentStockDetails.stockId, currentStockDetails.fullName, currentStockDetails.shortName))
        }
    }

    /* If quantity is positive it means user is getting back stocks as he cancelled an order so here reservedStocks will decrease
       when quantity is negative it means stocks reserved because he place new order so here reservedStocks will increase */
    fun updateReservedStocks(stockId: Int, quantity: Long) {
        for (currentStock in reservedStockDetails)
            if (currentStock.stockId == stockId) {
                currentStock.quantity -= quantity
                return
            }

        reservedStockDetails.add(StockDetails(stockId, -quantity)) // It means new stocks reserved
    }

    fun updateStocksOwned(stockId: Int, quantity: Long) {
        for (currentStock in ownedStockDetails)
            if (currentStock.stockId == stockId) {
                currentStock.quantity += quantity
                return
            }

        ownedStockDetails.add(StockDetails(stockId, quantity)) // It means new stock company added
    }

    fun getReservedStocksFromStockId(stockId: Int): Long {
        for (currentStock in reservedStockDetails) {
            if (currentStock.stockId == stockId)
                return currentStock.quantity
        }
        return 0L
    }

    fun updateFavouriteCompanyName(company: String) {
        companyName = company
    }

    fun updateDividendState(stockId: Int?, givesDividend: Boolean?) {
        if(stockId == null || givesDividend == null) return

        for(currentStock in globalStockDetails) {
            if(currentStock.stockId == stockId) {
                currentStock.givesDividend = givesDividend
                return
            }
        }
    }

    fun updateBankruptState(stockId: Int?, isBankrupt: Boolean?) {
        if(stockId == null || isBankrupt == null) return

        for(currentStock in globalStockDetails) {
            if(currentStock.stockId == stockId) {
                currentStock.isBankrupt = isBankrupt
                return
            }
        }
    }

    /* ============================= Stock Utils ============================= */

    fun getStockIdFromCompanyName(incomingCompanyName: String?): Int {
        for (stockIdCompanyName in stockIdCompanyNameList) {
            if (stockIdCompanyName.companyName.equals(incomingCompanyName, ignoreCase = true)) return stockIdCompanyName.stockId
        }
        return -1
    }

    fun getCompanyNameFromStockId(stockId: Int): String {
        for (stockIdCompanyName in stockIdCompanyNameList) {
            if (stockIdCompanyName.stockId == stockId) return stockIdCompanyName.companyName
        }
        return ""
    }

    fun getCompanyNameFromShortName(shortName: String): String? {
        for (stockIdCompanyName in stockIdCompanyNameList) {
            if (stockIdCompanyName.shortName == shortName) return stockIdCompanyName.companyName
        }
        return ""
    }

    fun getCompanyNamesArray(): MutableList<String> {
        val companyNames = mutableListOf<String>()
        for (i in 0 until stockIdCompanyNameList.size) {
            companyNames.add(stockIdCompanyNameList[i].companyName)
        }
        return companyNames
    }

    fun getShortNameForStockId(stockId: Int): String {
        for (currentDetails in stockIdCompanyNameList) {
            if (currentDetails.stockId == stockId) return currentDetails.shortName
        }
        return ""
    }

    fun getQuantityOwnedFromCompanyName(companyName: String?): Long {
        val stockId = getStockIdFromCompanyName(companyName)
        for ((stockId1, quantity) in ownedStockDetails) {
            if (stockId1 == stockId) return quantity
        }
        return 0
    }

    fun getQuantityOwnedFromStockId(stockId: Int): Long {
        for ((stockId1, quantity) in ownedStockDetails) {
            if (stockId1 == stockId) return quantity
        }
        return 0
    }

    fun getDescriptionFromCompanyName(companyName: String?): String? {
        val stockId = getStockIdFromCompanyName(companyName)
        for ((_, _, stockId1, description) in globalStockDetails) {
            if (stockId1 == stockId) return description
        }
        return ""
    }

    fun getImageUrlFromCompanyName(companyName: String?): String? {
        val stockId = getStockIdFromCompanyName(companyName)
        for ((_, _, stockId1, _, _, _, _, _, _, _, _, imagePath) in globalStockDetails) {
            if (stockId1 == stockId) return imagePath
        }
        return ""
    }

    fun getPriceFromCompanyName(companyName: String): Long {

        val stockId = getStockIdFromCompanyName(companyName)

        for ((_, _, stockId1, _, price) in globalStockDetails) {
            if (stockId1 == stockId) return price
        }
        return 0
    }

    fun getPriceFromStockId(stockId: Int): Long {

        for ((_, _, stockId1, _, price) in globalStockDetails) {
            if (stockId1 == stockId) return price
        }
        return 0
    }

    /**
     * To get the index of element by company name.
     * Used in spinner in the trade fragment to manually selected an item on it.
     */
    fun getIndexForFavoriteCompany(): Int {
        var index = 0

        if (companyName == null) return index

        while (index < getCompanyNamesArray().size) {
            if (companyName == getCompanyNamesArray()[index]) break
            index++
        }
        return index
    }

    fun getPreviousDayCloseFromStockId(list: List<GlobalStockDetails>, stockId: Int): Long {
        for ((_, _, stockId1, _, _, _, _, previousDayClose) in list) {
            if (stockId1 == stockId) return previousDayClose
        }
        return 0
    }
}