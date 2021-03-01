package org.pragyan.dalal18.data

import android.os.Parcelable
import dalalstreet.api.models.GameStateUpdateType
import dalalstreet.api.models.OrderType
import kotlinx.android.parcel.Parcelize
import java.util.*

data class CompanyDetails(var stockId: Int, var companyName: String, var shortName: String, var stockPrice: Long, var previousDayClose: Long, var isBankrupt: Boolean, var givesDividend: Boolean)

data class CompanyTickerDetails(val stockId: Int, val fullName: String, val imageUrl: String?, val previousDayClose: Long, val isUp: Boolean, var isBankrupt: Boolean, var givesDividend: Boolean)

@Parcelize
/* Modify definition according to needs; Refer Stock.proto for more attributes */
data class GlobalStockDetails(var fullName: String, var shortName: String, var stockId: Int, var description: String, var price: Long,
                              var quantityInMarket: Long, var quantityInExchange: Long, var previousDayClose: Long,
                              var up: Int, var isBankrupt: Boolean, var givesDividend: Boolean, val imagePath: String) : Parcelable

data class LeaderBoardDetails(val rank: Int, val name: String?, val stockWorth: Long, val wealth: Long, val isBlocked: Boolean)

data class MarketDepth(var price: Long, var volume: Long)

data class MortgageDetails(var stockId: Int, var shortName: String, var companyName: String, var stockQuantity: Long, var mortgagePrice: Long)

@Parcelize
data class NewsDetails(var createdAt: String?, var headlines: String?, var content: String?, var imagePath: String?) : Parcelable

data class Notification(val text: String, val createdAt: String)

data class Order(val orderId: Int, val isBid: Boolean, var isClosed: Boolean, val price: Long, val stockId: Int,
                 val companyName: String, val orderType: OrderType, val stockQuantity: Long, var stockQuantityFulfilled: Long) {
    fun incrementQuantityFulfilled(moreQuantityFulfilled: Long) {
        stockQuantityFulfilled += moreQuantityFulfilled
    }
}

data class Portfolio(val shortName: String, val quantityOwned: Long, val reservedStocks: Long, val worth: Long, val isBankrupt: Boolean, val givesDividend: Boolean)

@Parcelize
data class StockDetails(var stockId: Int, var quantity: Long) : Parcelable

data class StockHistory(var stockDate: Date?, var stockHigh: Long, var stockLow: Long, var stockOpen: Long, var stockClose: Long)

@Parcelize
data class GameStateDetails(val gameStateUpdateType: GameStateUpdateType, val isMarketOpen: Boolean?, val isOtpVerified: Boolean?, val dividendStockId: Int?, val givesDividend: Boolean?,
                            val bankruptStockId: Int?, val isBankrupt: Boolean?, val referredCashWorth: Long) : Parcelable

@Parcelize
data class CustomOrderUpdate(val orderId: Int, val isClosed: Boolean, val isAsk: Boolean, val orderPrice: Long, val companyName: String,
                             val stockId: Int, val stockQuantity: Long, val isNewOrder: Boolean, val orderType: OrderType) : Parcelable

