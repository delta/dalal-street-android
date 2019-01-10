package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Legend
import kotlinx.android.synthetic.main.fragment_worth.*

import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.ui.MainActivity

import java.util.ArrayList
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import org.pragyan.dalal18.utils.StockUtils.getCompanyNameFromStockId


class WorthFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_worth, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(piechart){
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(10f, 10f, 10f, 10f)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setDrawEntryLabels(false)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 57f
            transparentCircleRadius = 64f
            setDrawCenterText(true)
        }

        val legend = piechart.legend
        with(legend){
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 0f
            yOffset = 0f
            textColor = (ContextCompat.getColor(context!!,R.color.neutral_font_color))
        }

        setUpWorthPieChart()
    }

    private fun setUpWorthPieChart() {

        val entries = ArrayList<PieEntry>()

        for (currentStockDetails in MainActivity.ownedStockDetails) {

            var currentPrice = -1

            for (globalStockDetails in MainActivity.globalStockDetails) {
                if (currentStockDetails.stockId == globalStockDetails.stockId) {
                    currentPrice = globalStockDetails.price
                    break
                }
            }
            if (currentStockDetails.quantity != 0) {
                entries.add(PieEntry((currentPrice * currentStockDetails.quantity).toFloat(),getCompanyNameFromStockId(currentStockDetails.stockId)))
            }
        }
        val dataSet = PieDataSet(entries.toList(), "Total Worth")

        val colors = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())

        dataSet.colors = colors
        dataSet.selectionShift = 5f
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(R.color.neutral_font_color)
        piechart.data = data
        piechart.highlightValues(null)
        piechart.invalidate()
    }
}
