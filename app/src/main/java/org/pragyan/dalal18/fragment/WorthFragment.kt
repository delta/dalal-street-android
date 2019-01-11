package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.Legend
import kotlinx.android.synthetic.main.fragment_worth.*

import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent

import java.util.ArrayList
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieData
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.StockUtils.getCompanyNameFromStockId
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.animation.Easing
import org.pragyan.dalal18.data.StockDetails

class WorthFragment : Fragment() {

    private lateinit var model: DalalViewModel

    private lateinit var kreonLightTypeFace : Typeface

    private var totalStockWorth : Int = 0
    private var cashWorth :Int = 0
    private var totalWorth : Int = 0
    private var stocks : ArrayList<StockDetails> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_worth, container, false)
        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) } ?: throw Exception("Invalid activity")
        cashWorth = container?.rootView?.findViewById<TextView>(R.id.cashWorthTextView)?.text.toString().replace("," , "").toInt()
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.transactions)
        kreonLightTypeFace = ResourcesCompat.getFont(context!!, R.font.kreon_light)!!
        with(worthPiechart){
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(25f, 45f, 25f, 20f)
            isDrawHoleEnabled = true
            holeRadius = 65f
            setHoleColor(Color.WHITE)
            setDrawEntryLabels(false)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            transparentCircleRadius = 70f
            setDrawCenterText(true)
            centerText = "Total Worth \n (In percentage)"
            setCenterTextColor(R.color.black_background)
            setCenterTextSize(22f)
            setCenterTextTypeface(kreonLightTypeFace)
            animateY(800, Easing.EaseInOutQuad)
            isRotationEnabled = false
        }

        val legend = worthPiechart.legend
        with(legend){
            textSize = 18f
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 2f
            yOffset = -65f
            xOffset = 18f
            textColor = ContextCompat.getColor(context!!,R.color.gold_medal)
            typeface = kreonLightTypeFace
        }

        setUpWorthPieChart()
    }

    private fun setUpWorthPieChart() {

        val entries = ArrayList<PieEntry>()
        var currentPrice = -1
        val currentPriceList = ArrayList<Int>()

        for (currentStockDetails in model.ownedStockDetails) {
            for (globalStockDetails in model.globalStockDetails) {
                if (currentStockDetails.stockId == globalStockDetails.stockId) {
                    currentPrice = globalStockDetails.price
                    break
                }
            }

            if(currentStockDetails.quantity != 0){
                //Log.e("SAN", currentStockDetails.quantity * current)
                currentPriceList.add(currentPrice)
                totalStockWorth += currentPrice * currentStockDetails.quantity
                stocks.add(currentStockDetails)
            }
        }


        totalWorth = cashWorth + totalStockWorth

        var others = 0
        for(i in 0 until stocks.size){
            if((stocks[i].quantity * currentPriceList[i]) > (.02 * totalWorth)) {
                entries.add(PieEntry((stocks[i].quantity * currentPriceList[i]).toFloat(), getCompanyNameFromStockId(stocks[i].stockId)))
            } else {
                others += stocks[i].quantity * currentPriceList[i]
            }
        }
        if(others > 0){
            entries.add(PieEntry(others.toFloat(), "Others"))
        }
        entries.add(PieEntry(cashWorth.toFloat(),"Cash Worth"))

        val dataSet = PieDataSet(entries.toList(),"")

        val colors = ArrayList<Int>()
        //TODO:  Add more colors
        /*for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())*/

        colors.add(ContextCompat.getColor(context!!,R.color.neon_blue))
        colors.add(ContextCompat.getColor(context!!,R.color.neon_green))
        colors.add(ContextCompat.getColor(context!!,R.color.neon_purple))
        colors.add(ContextCompat.getColor(context!!,R.color.neon_yellow))
        colors.add(ContextCompat.getColor(context!!,R.color.neon_orange))
        colors.add(ContextCompat.getColor(context!!,R.color.neon_pink))

        dataSet.colors = colors
        dataSet.selectionShift = 5f
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(13f)
        data.setValueTypeface(kreonLightTypeFace)
        data.setValueTextColor(R.color.black)
        worthPiechart.data = data
        worthPiechart.highlightValues(null)
        worthPiechart.invalidate()
    }
}
