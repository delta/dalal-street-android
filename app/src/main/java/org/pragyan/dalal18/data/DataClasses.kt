package org.pragyan.dalal18.data

import android.os.Parcelable
import dalalstreet.api.models.GameStateUpdateType
import dalalstreet.api.models.OrderType
import kotlinx.android.parcel.Parcelize
import java.util.*

data class CompanyDetails(var company: String, var shortName: String, var value: Long, var previousDayClose: Long)

data class CompanyTickerDetails(val fullName: String, val imageUrl: String?, val previousDayClose: Long, val isUp: Boolean)

@Parcelize
/* Modify definition according to needs; Refer Stock.proto for more attributes */
data class GlobalStockDetails(var fullName: String, var shortName: String, var stockId: Int, var description: String, var price: Long,
                              var quantityInMarket: Long, var quantityInExchange: Long, var previousDayClose: Long,
                              var up: Int, val imagePath: String) : Parcelable

data class LeaderBoardDetails(var rank: Int, var name: String?, var stockWorth: Long, var wealth: Long)

data class MarketDepth(var price: Long, var volume: Long)

data class MortgageDetails(var stockId: Int, var companyName: String, var stockQuantity: Long, var mortgagePrice: Long)

@Parcelize
data class NewsDetails(var createdAt: String?, var headlines: String?, var content: String?, var imagePath: String?) : Parcelable

data class Notification(val text: String, val createdAt: String)

data class Order(val orderId: Int, val isBid: Boolean, var isClosed: Boolean, val price: Long, val stockId: Int,
                 val companyName: String, val orderType: OrderType, val stockQuantity: Long, var stockQuantityFulfilled: Long) {
    fun incrementQuantityFulfilled(moreQuantityFulfilled: Long) {
        stockQuantityFulfilled += moreQuantityFulfilled
    }
}

data class Portfolio(val shortName: String, var quantityOwned: Long, var reservedStocks: Long, var worth: Long)

@Parcelize
data class StockDetails(var stockId: Int, var quantity: Long) : Parcelable

data class StockHistory(var stockDate: Date?, var stockHigh: Long, var stockLow: Long, var stockOpen: Long, var stockClose: Long)

data class Transaction(var type: String?, var stockId: Int, val companyName: String, val noOfStocks: Long, val stockPrice: Long,
                       var time: String?, val totalMoney: Long)

data class StockIdCompanyName(val stockId: Int, val companyName: String, val shortName: String)

@Parcelize
data class GameStateDetails(val gameStateUpdateType: GameStateUpdateType, val isMarketOpen: Boolean?, val isOtpVerified: Boolean?, val dividendStockId: Int?, val givesDividend: Boolean?,
                            val bankruptStockId: Int?, val isBankrupt: Boolean?): Parcelable