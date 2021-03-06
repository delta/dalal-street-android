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
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.PortfolioRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.Portfolio
import org.pragyan.dalal18.data.StockDetails
import org.pragyan.dalal18.databinding.FragmentPortfolioBinding
import org.pragyan.dalal18.ui.MainActivity.Companion.GAME_STATE_UPDATE_ACTION
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

class PortfolioFragment : Fragment() {

    private var binding by viewLifecycle<FragmentPortfolioBinding>()

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
            if (intent.action != null && (intent.action == Constants.REFRESH_STOCK_PRICES_FOR_ALL ||
                            intent.action == Constants.REFRESH_OWNED_STOCKS_FOR_ALL ||
                            intent.action == GAME_STATE_UPDATE_ACTION)) {
                updatePortfolioTable()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPortfolioBinding.inflate(inflater, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        cashWorth = container?.rootView?.findViewById<TextView>(R.id.cashWorthTextView)?.text.toString().replace(",", "").toLong()
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.portfolioRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = portfolioRecyclerAdapter
            setHasFixedSize(false)
        }

        updatePortfolioTable()

        kreonLightTypeFace = ResourcesCompat.getFont(context!!, R.font.kreon_light)!!
        with(binding.portfolioPiechart) {
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

        val legend = binding.portfolioPiechart.legend
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
            currentPrice = model.getPriceFromStockId(stockId)

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
        if (others.toFloat() > 0)
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
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter())
        pieData.setValueTextSize(8f)
        pieData.setValueTypeface(kreonLightTypeFace)
        pieData.setValueTextColor(android.graphics.Color.WHITE)
        binding.portfolioPiechart.apply {
            data = pieData
            highlightValues(null)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter(Constants.REFRESH_OWNED_STOCKS_FOR_ALL)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_FOR_ALL)
        intentFilter.addAction(GAME_STATE_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshPortfolioDetails, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshPortfolioDetails)
    }

    private fun updatePortfolioTable() {

        val portfolioList = mutableListOf<Portfolio>()
        for ((stockId, quantity) in model.ownedStockDetails) {

            val currentPrice = model.getPriceFromStockId(stockId)

            if (quantity != 0L || model.getReservedStocksFromStockId(stockId) != 0L) {
                portfolioList.add(Portfolio(
                        model.getShortNameFromStockId(stockId),
                        quantity,
                        model.getReservedStocksFromStockId(stockId),
                        currentPrice * quantity,
                        model.getIsBankruptFromStockId(stockId),
                        model.getGivesDividendFromStockId(stockId)
                ))
            }
        }

        binding.apply {
            if (portfolioList.size > 0) {
                portfolioRecyclerAdapter.swapData(portfolioList)

                reservedCashTextView.text = DecimalFormat(Constants.PRICE_FORMAT).format(model.reservedCash)
                reservedStocksTextView.text = DecimalFormat(Constants.PRICE_FORMAT).format(model.getReservedStocksValue())

                portfolioScrollView.visibility = View.VISIBLE
                emptyPortfolioTextView.visibility = View.GONE
            } else {
                portfolioScrollView.visibility = View.GONE
                emptyPortfolioTextView.visibility = View.VISIBLE
            }
        }
    }
}
