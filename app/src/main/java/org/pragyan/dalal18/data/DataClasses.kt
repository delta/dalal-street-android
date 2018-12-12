package org.pragyan.dalal18.data

import android.os.Parcelable
import dalalstreet.api.datastreams.SubscriptionId
import kotlinx.android.parcel.Parcelize
import java.util.Date

data class CompanyDetails(var company: String?, var shortName: String?, var value: Int, var previousDayClose: Int)

data class CompanyTickerDetails(val fullName: String, val imageUrl: String?, val previousDayClose: Int, val isUp: Boolean)

@Parcelize
/* Modify definition according to needs; Refer Stock.proto for more attributes */
data class GlobalStockDetails(var fullName: String?, var shortName: String?, var stockId: Int, var price: Int, var quantityInMarket: Int,
                              var quantityInExchange: Int, var previousDayClose: Int, var up: Int, val imagePath: String) : Parcelable

data class LeaderBoardDetails(var rank: Int, var name: String?, var wealth: Int)

data class MarketDepth(var price: Int, var volume: Int)

@Parcelize
data class NewsDetails(var createdAt: String?, var headlines: String?, var content: String?, var imagePath: String?) : Parcelable

data class Notification(val text: String, val createdAt: String)

data class Order(val orderId: Int, val isBid: Boolean, var isClosed: Boolean, val price: Int, val stockId: Int, val orderType: Int,
                 val stockQuantity: Int, var stockQuantityFulfilled: Int)

data class Portfolio(val shortName: String, var companyName: String?, var quantityOwned: Int, var price: Int, var previousDayClose: Int)

data class RegistrationDetails(val fullName: String, val password: String, val username: String, val country: String, val email: String)

@Parcelize
data class StockDetails(var stockId: Int, var quantity: Int) : Parcelable

data class StockHistory(var stockDate: Date?, var stockClose: Int)

data class Transaction(var type: String?, var stockId: Int, val noOfStocks: Int, val stockPrice: Float,
                       var time: String?, val totalMoney: Float)

data class Subscription(val type: SubscriptionType, val subscriptionId: SubscriptionId) {

    enum class SubscriptionType {
        TRANSACTIONS,
        STOCK_PRICES,
        MARKET_EVENTS,
        STOCK_EXCHANGE
    }
}