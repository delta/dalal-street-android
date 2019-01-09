package org.pragyan.dalal18.fragment.marketDepth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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
import org.pragyan.dalal18.data.StockHistory
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
import org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate
import org.pragyan.dalal18.utils.StockUtils
import org.pragyan.dalal18.utils.StockUtils.getStockIdFromCompanyName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DepthGraphFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var xVals = ArrayList<String>()
    private var yVals = ArrayList<Entry>()
    private var stockHistoryList = ArrayList<StockHistory>()
    private var trimmedStockHistoryList = ArrayList<StockHistory>()
    private lateinit var lineDataSet: LineDataSet

    private var loadingDialog: AlertDialog? = null
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement network down handler.")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_depth_graph, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
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

        with(market_depth_chart) {
            backgroundColor = ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.black_background)
            setDrawGridBackground(false)
            setBorderColor(ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.neutral_font_color))
            setTouchEnabled(false)
            setNoDataText("Select a company to view depth chart")
            description.isEnabled = false
            setGridBackgroundColor(ContextCompat.getColor(this@DepthGraphFragment.context!!, R.color.neutral_font_color))
        }

        val arrayAdapter = ArrayAdapter(activity!!, R.layout.company_spinner_item, StockUtils.getCompanyNamesArray())
        with(graph_company_spinner) {
            setAdapter(arrayAdapter)
            isSelected = false
            setOnItemClickListener { _, _, _, _ ->
                val currentCompany = graph_company_spinner.text.toString()
                stockHistoryList.clear()
                trimmedStockHistoryList.clear()
                xVals.clear()
                yVals.clear()
                if (!market_depth_chart.isEmpty) {
                    market_depth_chart.invalidate()
                    market_depth_chart.clear()
                }
                market_depth_chart.clearFocus()
                if (activity != null && isAdded) {
                    loadingDialog?.show()
                    loadStockHistoryAsynchronously(currentCompany)
                }
            }
        }
    }

    private fun loadStockHistoryAsynchronously(currentCompany: String) {

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val stockHistoryResponse = actionServiceBlockingStub.getStockHistory(GetStockHistoryRequest
                        .newBuilder()
                        .setStockId(getStockIdFromCompanyName(currentCompany))
                        .setResolution(StockHistoryResolution.SixtyMinutes)
                        .build())

                uiThread {
                    for (map in stockHistoryResponse.stockHistoryMapMap.entries) {
                        val tempStockHistory = StockHistory(convertToDate(map.key), map.value.close)
                        stockHistoryList.add(tempStockHistory)
                    }
                    stockHistoryList.sortWith(kotlin.Comparator { (date1), (date2) -> date1!!.compareTo(date2) })
                    stockHistoryList.reverse()

                    if (stockHistoryList.size >= 10) {
                        for (i in 0..9) {
                            trimmedStockHistoryList.add(stockHistoryList[i])
                        }
                    } else {
                        trimmedStockHistoryList.addAll(stockHistoryList)
                    }

                    var highestClose = 0f
                    trimmedStockHistoryList.reverse()
                    for (i in trimmedStockHistoryList.indices) {
                        xVals.add(parseDate(convertToString(trimmedStockHistoryList[i].stockDate)))
                        yVals.add(Entry(i.toFloat(), trimmedStockHistoryList[i].stockClose.toFloat()))
                        if (highestClose <= trimmedStockHistoryList[i].stockClose.toFloat()) {
                            highestClose = trimmedStockHistoryList[i].stockClose.toFloat()
                        }
                    }

                    val xValsArray: Array<String?> = arrayOfNulls(xVals.size)
                    for (i in 0 until xVals.size) {
                        xValsArray[i] = xVals[i]
                    }

                    val formatter = IAxisValueFormatter { value, _ -> xValsArray[value.toInt()] }
                    val xAxis = market_depth_chart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = formatter
                    xAxis.granularity = 1f
                    xAxis.textSize = 9f
                    xAxis.textColor = ContextCompat.getColor(context!!, android.R.color.white)
                    xAxis.enableGridDashedLine(15f, 10f, 0f)

                    var yAxis = market_depth_chart.axisRight
                    yAxis.isEnabled = false
                    yAxis = market_depth_chart.axisLeft
                    yAxis.granularity = 1f
                    yAxis.enableGridDashedLine(15f, 10f, 0f)
                    yAxis.axisMaximum = (highestClose + (0.2 * highestClose)).toFloat()
                    yAxis.axisMinimum = 0f
                    yAxis.textSize = 9f
                    yAxis.textColor = ContextCompat.getColor(context!!, android.R.color.white)


                    if (activity != null && isAdded) {
                        lineDataSet = LineDataSet(yVals, "Stock Price")
                        lineDataSet.lineWidth = 4f
                        lineDataSet.color = ContextCompat.getColor(context!!, R.color.neon_green)
                        lineDataSet.setCircleColor(ContextCompat.getColor(context!!, R.color.redTint))
                        lineDataSet.circleRadius = MiscellaneousUtils.convertDpToPixel(context!!, 1f)
                        lineDataSet.highLightColor = ContextCompat.getColor(context!!, R.color.neon_green)
                        lineDataSet.setDrawFilled(false)
                        lineDataSet.valueTextColor = ContextCompat.getColor(context!!, R.color.neon_blue)
                        lineDataSet.valueTextSize = MiscellaneousUtils.convertDpToPixel(context!!, 4f)

                        val datasets = ArrayList<ILineDataSet>()
                        datasets.add(lineDataSet)
                        val lineData = LineData(datasets)

                        market_depth_chart.data = lineData
                        market_depth_chart.invalidate()
                        loadingDialog?.dismiss()

                        val legend = market_depth_chart.legend
                        legend.textColor = ContextCompat.getColor(context!!, android.R.color.white)
                        legend.textSize = MiscellaneousUtils.convertDpToPixel(context!!, 3f)
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                        legend.form = Legend.LegendForm.LINE
                    }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
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

    override fun onPause() {
        super.onPause()
        stockHistoryList.clear()
        trimmedStockHistoryList.clear()
        xVals.clear()
        yVals.clear()
    }
}
