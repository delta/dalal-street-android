package org.pragyan.dalal18.data

import androidx.lifecycle.ViewModel
import org.pragyan.dalal18.utils.StockUtils.StockIdCompanyName
import org.pragyan.dalal18.utils.StockUtils.stockIdCompanyNameList
import java.util.*

class DalalViewModel : ViewModel() {

    lateinit var ownedStockDetails: MutableList<StockDetails>
    lateinit var globalStockDetails: MutableList<GlobalStockDetails>

    fun updateGlobalStock(position: Int, price: Int, quantityInMarket: Int, quantityInExchange: Int) {
        globalStockDetails[position].price = price
        globalStockDetails[position].quantityInMarket = quantityInMarket
        globalStockDetails[position].quantityInExchange = quantityInExchange
    }

    fun updateGlobalStockPrice(pos: Int, price: Int) {
        globalStockDetails[pos-1].price = price
    }

    fun addNewOwnedStock(stock: StockDetails) {
        ownedStockDetails.add(stock)
    }

    fun createCompanyArrayFromGlobalStockDetails() {
        stockIdCompanyNameList = ArrayList<StockIdCompanyName>()

        for (currentStockDetails in globalStockDetails) {
            stockIdCompanyNameList.add(StockIdCompanyName(currentStockDetails.stockId, currentStockDetails.fullName, currentStockDetails.shortName))
        }
    }
}