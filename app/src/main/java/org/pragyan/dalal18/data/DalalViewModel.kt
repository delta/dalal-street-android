package org.pragyan.dalal18.data

import androidx.lifecycle.ViewModel

class DalalViewModel : ViewModel() {

    lateinit var ownedStockDetails: MutableList<StockDetails>
    lateinit var globalStockDetails: HashMap<Int, GlobalStockDetails>
    lateinit var reservedStockDetails: MutableList<StockDetails>

    var companyName: String? = null

    var reservedCash = 0L

    fun updateGlobalStock(stockId: Int, price: Long, quantityInMarket: Long, quantityInExchange: Long) {
        globalStockDetails[stockId]?.price = price
        globalStockDetails[stockId]?.quantityInMarket = quantityInMarket
        globalStockDetails[stockId]?.quantityInExchange = quantityInExchange
    }

    fun updateGlobalStockPrice(stockId: Int, price: Long) {
        globalStockDetails[stockId]?.price = price
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
        if (stockId == null || givesDividend == null) return
        globalStockDetails[stockId]?.givesDividend = givesDividend

    }

    fun updateBankruptState(stockId: Int?, isBankrupt: Boolean?) {
        if (stockId == null || isBankrupt == null) return
        globalStockDetails[stockId]?.isBankrupt = isBankrupt
    }

    fun getGlobalStockPriceFromStockId(stockId: Int): Long {
        return globalStockDetails[stockId]?.price ?: 0
    }

    /* ============================= Stock Utils ============================= */

    fun getStockIdFromCompanyName(incomingCompanyName: String?): Int {
        for ((stockId, currentStock) in globalStockDetails) {
            if (currentStock.fullName.equals(incomingCompanyName, ignoreCase = true)) return stockId
        }
        return -1
    }

    fun getCompanyNameFromStockId(stockId: Int): String {
        return globalStockDetails[stockId]?.fullName ?: ""
    }

    fun getCompanyNamesArray(): MutableList<String> {
        val companyNames = mutableListOf<String>()
        for ((_, currentStock) in globalStockDetails) {
            companyNames.add(currentStock.fullName)
        }
        return companyNames
    }

    fun getShortNameFromStockId(stockId: Int): String {
        return globalStockDetails[stockId]?.shortName ?: ""
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
        for ((_, currentStock) in globalStockDetails) {
            if (currentStock.fullName == companyName) return currentStock.description
        }
        return ""
    }

    fun getImageUrlFromCompanyName(companyName: String?): String? {
        for ((_, currentStock) in globalStockDetails) {
            if (currentStock.fullName == companyName) return currentStock.imagePath
        }
        return ""
    }

    fun getPriceFromCompanyName(companyName: String): Long {
        for ((_, currentStock) in globalStockDetails) {
            if (currentStock.fullName == companyName) return currentStock.price
        }
        return 0
    }

    fun getPriceFromStockId(stockId: Int): Long {
        return globalStockDetails[stockId]?.price ?: 0
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