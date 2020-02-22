package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.fragment_portfolio.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.PortfolioRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.Portfolio
import org.pragyan.dalal18.data.StockDetails
import org.pragyan.dalal18.utils.Constants
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject


class PortfolioFragment : Fragment() {

    @Inject
    lateinit var portfolioRecyclerAdapter: PortfolioRecyclerAdapter

    private lateinit var model: DalalViewModel

    private lateinit var kreonLightTypeFace: Typeface

    private var totalStockWorth: Long = 0L
    private var cashWorth: Long = 0
    private var totalWorth: Long = 0
    private var stocks: ArrayList<StockDetails> = ArrayList()

    private val refreshPortfolioDetails = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && (intent.action == Constants.REFRESH_STOCK_PRICES_FOR_ALL || intent.action == Constants.REFRESH_OWNED_STOCKS_FOR_ALL)) {
                updatePortfolioTable()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_portfolio, container, false)
        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        cashWorth = container?.rootView?.findViewById<TextView>(R.id.cashWorthTextView)?.text.toString().replace(",", "").toLong()
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.portfolio)
        with(portfolioRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = portfolioRecyclerAdapter
            setHasFixedSize(false)
        }

        updatePortfolioTable()

        kreonLightTypeFace = ResourcesCompat.getFont(context!!, R.font.kreon_light)!!
        with(portfolio_piechart) {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(25f, 45f, 25f, 20f)
            isDrawHoleEnabled = true
            holeRadius = 60f
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setDrawEntryLabels(false)
            setTransparentCircleColor(android.graphics.Color.WHITE)
            setTransparentCircleAlpha(95)
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            centerText = "Net Worth \n (In percentage)"
            setCenterTextColor(android.graphics.Color.WHITE)
            setCenterTextSize(22f)
            setCenterTextTypeface(kreonLightTypeFace)
            animateY(800, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            isRotationEnabled = false
        }

        val legend = portfolio_piechart.legend
        with(legend) {
            textSize = 18f
            verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 2f
            yOffset = -65f
            xOffset = 18f
            textColor = ContextCompat.getColor(context!!, R.color.gold_medal)
            typeface = kreonLightTypeFace
        }

        setUpWorthPieChart()
    }

    private fun setUpWorthPieChart() {

        val entries = ArrayList<PieEntry>()
        var currentPrice: Long
        val currentPriceList = ArrayList<Long>()

        for ((stockId, quantity) in model.ownedStockDetails) {
            currentPrice = model.getGlobalStockPriceFromStockId(stockId)

            if (quantity > 0L) {
                currentPriceList.add(currentPrice)
                totalStockWorth += currentPrice * quantity
                stocks.add(StockDetails(stockId, quantity))
            }
        }


        totalWorth = cashWorth + totalStockWorth

        var others = 0L
        for (i in 0 until stocks.size) {
            if ((stocks[i].quantity * currentPriceList[i]) > (.02 * totalWorth)) {
                entries.add(PieEntry((stocks[i].quantity * currentPriceList[i]).toFloat(), model.getCompanyNameFromStockId(stocks[i].stockId)))
            } else {
                others += stocks[i].quantity * currentPriceList[i]
            }
        }

        if (cashWorth > (.02 * totalWorth)) {
            entries.add(PieEntry(cashWorth.toFloat(), "Cash Worth"))
        } else {
            others += cashWorth
        }
        if(others.toFloat() > 0)
            entries.add(PieEntry(others.toFloat(), "Others"))


        val dataSet = PieDataSet(entries.toList(), "")
        val colors = ArrayList<Int>()

        colors.add(ContextCompat.getColor(context!!, R.color.blue_piechart))
        colors.add(ContextCompat.getColor(context!!, R.color.red_piechart))
        colors.add(ContextCompat.getColor(context!!, R.color.green_piechart))
        colors.add(ContextCompat.getColor(context!!, R.color.gold_medal))
        colors.add(ContextCompat.getColor(context!!, R.color.neon_blue))
        colors.add(ContextCompat.getColor(context!!, R.color.green))
        colors.add(ContextCompat.getColor(context!!, R.color.neon_yellow))
        colors.add(ContextCompat.getColor(context!!, R.color.lime))
        colors.add(ContextCompat.getColor(context!!, R.color.neon_orange))
        colors.add(ContextCompat.getColor(context!!, R.color.bronze_medal))
        colors.add(ContextCompat.getColor(context!!, R.color.neon_pink))
        colors.add(ContextCompat.getColor(context!!, R.color.redTint))


        dataSet.colors = colors
        dataSet.selectionShift = 5f
        dataSet.sliceSpace = 1f
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(8f)
        data.setValueTypeface(kreonLightTypeFace)
        data.setValueTextColor(android.graphics.Color.WHITE)
        portfolio_piechart.data = data
        portfolio_piechart.highlightValues(null)
        portfolio_piechart.invalidate()
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter(Constants.REFRESH_OWNED_STOCKS_FOR_ALL)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_FOR_ALL)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshPortfolioDetails, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshPortfolioDetails)
    }

    private fun updatePortfolioTable() {

        val portfolioList = mutableListOf<Portfolio>()
        for ((stockId, quantity) in model.ownedStockDetails) {

            val currentPrice = model.getGlobalStockPriceFromStockId(stockId)

            if (quantity != 0L) {
                portfolioList.add(Portfolio(
                        model.getShortNameFromStockId(stockId),
                        quantity,
                        model.getReservedStocksFromStockId(stockId),
                        currentPrice * quantity
                ))
            }
        }

        if (portfolioList.size > 0) {
            portfolioRecyclerAdapter.swapData(portfolioList)

            val tempString = "Reserved Cash: " + Constants.RUPEE_SYMBOL + DecimalFormat(Constants.PRICE_FORMAT).format(model.reservedCash)
            reservedCashTextView.text = tempString

            portfolioScrollView.visibility = View.VISIBLE
            emptyPortfolioRelativeLayout.visibility = View.GONE
        } else {
            portfolioScrollView.visibility = View.GONE
            emptyPortfolioRelativeLayout.visibility = View.VISIBLE
        }
    }
}