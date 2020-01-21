package org.pragyan.dalal18.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.pragyan.dalal18.utils.StockUtils.StockIdCompanyName
import org.pragyan.dalal18.utils.StockUtils.stockIdCompanyNameList
import java.util.*

class DalalViewModel : ViewModel() {

    lateinit var ownedStockDetails: MutableList<StockDetails>
    lateinit var globalStockDetails: MutableList<GlobalStockDetails>
    lateinit var reservedStockDetails: MutableList<StockDetails>
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
        stockIdCompanyNameList = ArrayList<StockIdCompanyName>()

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

    fun updateCompanySelectedMarketDepth(company: String) {
        companyName = company
    }
}