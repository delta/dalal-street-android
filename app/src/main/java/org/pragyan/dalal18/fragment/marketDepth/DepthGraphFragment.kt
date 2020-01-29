package org.pragyan.dalal18.fragment.marketDepth

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetStockHistoryRequest
import dalalstreet.api.actions.StockHistoryResolution
import kotlinx.android.synthetic.main.fragment_depth_graph.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.StockHistory
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DepthGraphFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel

    private var xVals = ArrayList<String>()
    private var yVals = ArrayList<CandleEntry>()
    private lateinit var stockHistoryList: ArrayList<StockHistory>

    private var loadingDialog: AlertDialog? = null
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private var currentCompany: String? = null
    private var currentInterval: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_depth_graph, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_depth)
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        stockHistoryList = ArrayList()
        with(market_depth_chart) {
            backgroundColor = ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.black_background)
            setTouchEnabled(false)
            setNoDataText("Select a company to view depth chart")
            description.isEnabled = true

            description.textColor = ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.neutral_font_color)
            setPinchZoom(true)
            setDrawGridBackground(false)
            legend.isEnabled = false
        }

        val arrayAdapter = ArrayAdapter(activity!!, R.layout.company_spinner_item, model.getCompanyNamesArray())
        with(graph_company_spinner) {
            setAdapter(arrayAdapter)
            isSelected = false
            setOnItemClickListener { _, _, _, _ ->
                currentCompany = graph_company_spinner.text.toString()
                stockHistoryList.clear()
                xVals.clear()
                yVals.clear()
                if (!market_depth_chart.isEmpty) {
                    market_depth_chart.invalidate()
                    market_depth_chart.clear()
                }
                market_depth_chart.clearFocus()
                if (activity != null && isAdded) {
                    loadStockHistoryAsynchronously()
                }
            }
        }

        val intervalAdapter = ArrayAdapter(activity!!, R.layout.interval_spinner_item, resources.getStringArray(R.array.intervalType))
        currentInterval = "30 mins"
        with(graph_time_spinner) {
            setAdapter(intervalAdapter)
            isSelected = false
            setOnItemClickListener { _, _, _, _ ->
                currentInterval = graph_time_spinner.text.toString()
                hint = "30 mins"
                stockHistoryList.clear()
                xVals.clear()
                yVals.clear()
                if (!market_depth_chart.isEmpty) {
                    market_depth_chart.invalidate()
                    market_depth_chart.clear()
                }
                market_depth_chart.clearFocus()
                if (activity != null && isAdded) {
                    loadStockHistoryAsynchronously()
                }
            }
        }
    }

    private fun loadStockHistoryAsynchronously() {

        if (currentCompany == null || currentInterval == null) {
            return
        }

        lateinit var resolution: StockHistoryResolution
        when (currentInterval) {

            "1 min" -> {
                resolution = StockHistoryResolution.OneMinute
            }

            "5 mins" -> {
                resolution = StockHistoryResolution.FiveMinutes
            }

            "15 mins" -> {
                resolution = StockHistoryResolution.FifteenMinutes
            }

            "30 mins" -> {
                resolution = StockHistoryResolution.ThirtyMinutes
            }

            "60 mins" -> {
                resolution = StockHistoryResolution.SixtyMinutes
            }

            else -> {
                resolution = StockHistoryResolution.OneMinute
            }
        }
        loadingDialog?.show()
        doAsync {

            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val stockHistoryResponse = actionServiceBlockingStub.getStockHistory(GetStockHistoryRequest
                            .newBuilder()
                            .setStockId(model.getStockIdFromCompanyName(currentCompany))
                            .setResolution(resolution)
                            .build())

                    uiThread {

                        for (map in stockHistoryResponse.stockHistoryMapMap.entries) {
                            val tempStockHistory = StockHistory(convertToDate(map.key), map.value.high, map.value.low, map.value.open, map.value.close)
                            stockHistoryList.add(tempStockHistory)
                        }
                        stockHistoryList.sortWith(kotlin.Comparator { (date1), (date2) -> date1!!.compareTo(date2) })

                        var highestClose = 0f
                        for (i in stockHistoryList.indices) {
                            xVals.add(parseDate(convertToString(stockHistoryList[i].stockDate)))
                            yVals.add(CandleEntry(i.toFloat(), stockHistoryList[i].stockHigh.toFloat(), stockHistoryList[i].stockLow.toFloat(),
                                    stockHistoryList[i].stockOpen.toFloat(), stockHistoryList[i].stockClose.toFloat()))
                            if (highestClose <= stockHistoryList[i].stockClose.toFloat()) {
                                highestClose = stockHistoryList[i].stockClose.toFloat()
                            }
                        }

                        val xValsArray: Array<String?> = arrayOfNulls(xVals.size)
                        for (i in 0 until xVals.size) {
                            xValsArray[i] = xVals[i]
                        }
                        val formatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                                return if (value.toInt() < xValsArray.size) {
                                    xValsArray[value.toInt()] ?: ""
                                } else {
                                    xValsArray[xValsArray.size - 1] ?: ""
                                }
                            }
                        }
                        val xAxis = market_depth_chart.xAxis
                        with(xAxis) {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            valueFormatter = formatter
                            textSize = 9f
                            textColor = ContextCompat.getColor(this@DepthGraphFragment.context!!, android.R.color.white)
                            setDrawGridLines(true)
                            granularity = 1f
                            labelCount = 3
                            setAvoidFirstLastClipping(true)
                        }

                        val leftAxis = market_depth_chart.axisLeft
                        leftAxis.isEnabled = false
                        val yAxis = market_depth_chart.axisRight
                        with(yAxis) {
                            setLabelCount(7, false)
                            setDrawGridLines(false)
                            setDrawAxisLine(true)
                            textSize = 9f
                            textColor = ContextCompat.getColor(this@DepthGraphFragment.context!!, android.R.color.white)
                            setDrawGridLines(true)
                            axisMaximum = (highestClose + (0.2 * highestClose)).toFloat()
                            axisMinimum = 0f
                        }

                        val set1 = CandleDataSet(yVals, "Stock Price")
                        with(set1) {
                            color = Color.rgb(80, 80, 80)
                            shadowColor = ContextCompat.getColor(context!!, android.R.color.white)
                            shadowWidth = 0.5f
                            decreasingColor = ContextCompat.getColor(context!!, R.color.redTint)
                            decreasingPaintStyle = Paint.Style.FILL
                            increasingColor = ContextCompat.getColor(context!!, R.color.neon_green)
                            increasingPaintStyle = Paint.Style.FILL
                            neutralColor = ContextCompat.getColor(context!!, R.color.neon_yellow)
                            setDrawValues(false)
                        }

                        val data = CandleData(set1)
                        market_depth_chart.data = data
                        market_depth_chart.invalidate()
                        market_depth_chart.description.text = "($currentInterval)"
                        graph_time_spinner.requestFocus()
                        graph_company_spinner.requestFocus()
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.market_depth_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.market_depth_dest) }
            }
            uiThread { loadingDialog?.dismiss() }
        }
    }

    private fun convertToDate(stringDate: String): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        var date: Date? = null
        try {
            date = format.parse(stringDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    private fun convertToString(date: Date?): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        return df.format(date)
    }
}