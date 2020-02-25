package org.pragyan.dalal18.data

import androidx.lifecycle.ViewModel

class DalalViewModel : ViewModel() {

    lateinit var ownedStockDetails: HashMap<Int, Long>
    lateinit var globalStockDetails: HashMap<Int, GlobalStockDetails>
    lateinit var reservedStockDetails: HashMap<Int, Long>
    lateinit var mortgageStockDetails: HashMap<Pair<Int, Long>, Long>

    var favoriteCompanyStockId: Int? = null

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
        reservedStockDetails[stockId] = reservedStockDetails[stockId]?.plus(quantity) ?: quantity
    }

    fun updateStocksOwned(stockId: Int, quantity: Long) {
        ownedStockDetails[stockId] = ownedStockDetails[stockId]?.plus(quantity) ?: quantity
    }

    fun updateMortgagedStocks(stockId: Int, quantity: Long, price: Long) {
        val stockIdPricePair = Pair(stockId, price)
        mortgageStockDetails[stockIdPricePair] = mortgageStockDetails[stockIdPricePair]?.plus(quantity)
                ?: quantity
    }

    fun getReservedStocksFromStockId(stockId: Int): Long {
        return reservedStockDetails[stockId] ?: 0
    }

    fun updateFavouriteCompanyStockId(stockId: Int) {
        favoriteCompanyStockId = stockId
    }

    fun updateDividendState(stockId: Int?, givesDividend: Boolean?) {
        if (stockId == null || givesDividend == null) return
        globalStockDetails[stockId]?.givesDividend = givesDividend

    }

    fun updateBankruptState(stockId: Int?, isBankrupt: Boolean?) {
        if (stockId == null || isBankrupt == null) return
        globalStockDetails[stockId]?.isBankrupt = isBankrupt
    }

    fun getReservedStocksValue(): Long {
        var reservedStocksValue = 0L

        for((stockId, quantity) in reservedStockDetails) {
            reservedStocksValue += (getPriceFromStockId(stockId) * quantity)
        }

        return reservedStocksValue
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

    fun getQuantityOwnedFromStockId(stockId: Int): Long {
        return ownedStockDetails[stockId] ?: 0
    }

    fun getDescriptionFromStockId(stockId: Int): String {
        return globalStockDetails[stockId]?.description ?: ""
    }

    fun getImageUrlFromStockId(stockId: Int): String {
        return globalStockDetails[stockId]?.imagePath ?: ""
    }

    fun getGivesDividendFromStockId(stockId: Int): Boolean {
        return globalStockDetails[stockId]?.givesDividend ?: false
    }

    fun getIsBankruptFromStockId(stockId: Int): Boolean {
        return globalStockDetails[stockId]?.isBankrupt ?: false
    }

    fun getPriceFromStockId(stockId: Int): Long {
        return globalStockDetails[stockId]?.price ?: 0
    }

    fun getMortgagedStocksFromStockId(stockId: Int): Long {

        var stocks = 0L
        for ((pair, quantity) in mortgageStockDetails) {
            if (pair.first == stockId) stocks += quantity
        }
        return stocks
    }

    /**
     * To get the index of element by company name.
     * Used in spinner in the trade fragment to manually selected an item on it.
     */
    fun getIndexForFavoriteCompany(): Int {
        var index = 0
        val favoriteId = favoriteCompanyStockId ?: return index

        while (index < getCompanyNamesArray().size) {
            if (getCompanyNameFromStockId(favoriteId) == getCompanyNamesArray()[index]) break
            index++
        }
        return index
    }
}