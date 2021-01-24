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
import kotlinx.coroutines.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.StockHistory
import org.pragyan.dalal18.databinding.FragmentDepthGraphBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate
import org.pragyan.dalal18.utils.toast
import org.pragyan.dalal18.utils.viewLifecycle
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DepthGraphFragment : Fragment() {

    private var binding by viewLifecycle<FragmentDepthGraphBinding>()

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
        binding = FragmentDepthGraphBinding.inflate(inflater, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

        return binding.root
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
        with(binding.marketDepthChart) {
            setBackgroundColor(ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.black_background))
            setTouchEnabled(false)
            setNoDataText("Select a company to view depth chart")
            description.isEnabled = true

            description.textColor = ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.neutral_font_color)
            setPinchZoom(true)
            setDrawGridBackground(false)
            legend.isEnabled = false
        }

        val arrayAdapter = ArrayAdapter(activity!!, R.layout.company_spinner_item, model.getCompanyNamesArray())
        with(binding.graphCompanySpinner) {
            setAdapter(arrayAdapter)
            isSelected = false
            setOnItemClickListener { _, _, _, _ ->
                currentCompany = text.toString()
                stockHistoryList.clear()
                xVals.clear()
                yVals.clear()
                binding.marketDepthChart.apply {
                    if (!isEmpty) {
                        invalidate()
                        clear()
                    }
                    clearFocus()
                }
                if (activity != null && isAdded) {
                    loadStockHistoryAsynchronously()
                }
            }
        }

        val intervalAdapter = ArrayAdapter(activity!!, R.layout.interval_spinner_item, resources.getStringArray(R.array.intervalType))
        currentInterval = "30 mins"
        with(binding.graphTimeSpinner) {
            setAdapter(intervalAdapter)
            isSelected = false
            setOnItemClickListener { _, _, _, _ ->
                currentInterval = text.toString()
                hint = "30 mins"
                stockHistoryList.clear()
                xVals.clear()
                yVals.clear()
                binding.marketDepthChart.apply {
                    if (!isEmpty) {
                        invalidate()
                        clear()
                    }
                    clearFocus()
                }
                if (activity != null && isAdded) {
                    loadStockHistoryAsynchronously()
                }
            }
        }
    }

    private fun loadStockHistoryAsynchronously() {
        if (currentCompany == null || currentInterval == null) return

        val resolution = when (currentInterval) {
            "1 min" -> StockHistoryResolution.OneMinute
            "5 mins" -> StockHistoryResolution.FiveMinutes
            "15 mins" -> StockHistoryResolution.FifteenMinutes
            "30 mins" -> StockHistoryResolution.ThirtyMinutes
            "60 mins" -> StockHistoryResolution.SixtyMinutes
            else -> StockHistoryResolution.OneMinute
        }

        loadingDialog?.show()
        GlobalScope.async (Dispatchers.Default){

            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val stockHistoryResponse = actionServiceBlockingStub.getStockHistory(GetStockHistoryRequest
                            .newBuilder()
                            .setStockId(model.getStockIdFromCompanyName(currentCompany))
                            .setResolution(resolution)
                            .build())

                    withContext(Dispatchers.Main) {

                        for (map in stockHistoryResponse.stockHistoryMapMap.entries) {
                            val tempStockHistory = StockHistory(convertToDate(map.key), map.value.high, map.value.low, map.value.open, map.value.close)
                            stockHistoryList.add(tempStockHistory)
                        }
                        stockHistoryList.sortWith(kotlin.Comparator { (date1), (date2) -> date1!!.compareTo(date2) })

                        var highestClose = 0f
                        if (stockHistoryList.size != 0) {
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
                            with(binding.marketDepthChart.xAxis) {
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

                            val leftAxis = binding.marketDepthChart.axisLeft
                            leftAxis.isEnabled = false
                            with(binding.marketDepthChart.axisRight) {
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
                            binding.apply {
                                marketDepthChart.data = data
                                marketDepthChart.invalidate()
                                marketDepthChart.description.text = "($currentInterval)"
                                graphTimeSpinner.requestFocus()
                                graphCompanySpinner.requestFocus()
                            }
                        } else {
                            context?.toast("No data available for this interval")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.market_depth_dest) }
                }
            } else {
                withContext(Dispatchers.Main) { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.market_depth_dest) }
            }
            withContext(Dispatchers.Main) { loadingDialog?.dismiss() }
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
