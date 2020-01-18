package org.pragyan.dalal18.fragment

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_companies.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.CompanyRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.CompanyDetails
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.Constants.PREF_COMP
import org.pragyan.dalal18.utils.DalalTourUtils
import java.util.*
import javax.inject.Inject

class CompanyFragment : Fragment(), CompanyRecyclerAdapter.OnCompanyClickListener {

    @Inject
    lateinit var preferences: SharedPreferences

    private val companiesList = ArrayList<CompanyDetails>()
    private lateinit var adapter: CompanyRecyclerAdapter
    private lateinit var model: DalalViewModel

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Constants.REFRESH_STOCK_PRICES_ACTION, ignoreCase = true) ||
                    intent.action.equals(Constants.REFRESH_STOCKS_EXCHANGE_ACTION, ignoreCase = true)) {
                updateValues()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_companies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.companies)

        adapter = CompanyRecyclerAdapter(context, null, this)

        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        updateValues()
        portfolioRecyclerView.setHasFixedSize(false)
        portfolioRecyclerView.adapter = adapter
        portfolioRecyclerView.layoutManager = LinearLayoutManager(context)

        if (preferences.getBoolean(PREF_COMP, true)) {
            preferences.edit().putBoolean(PREF_COMP, false).apply()
            DalalTourUtils.genericViewTour(activity as AppCompatActivity, percentageChangeTextView, 100, getString(R.string.percentchange_tour))
        }
    }

    fun updateValues() {

        companiesList.clear()

        for ((fullName, shortName, _, _, price, _, _, previousDayClose) in model.globalStockDetails) {
            companiesList.add(CompanyDetails(fullName, shortName, price, previousDayClose))
        }

        adapter.swapData(companiesList)
    }

    override fun onCompanyClick(view: View, position: Int) {
        val bundle = Bundle()
        bundle.putString(CompanyDescriptionFragment.COMPANY_NAME_KEY, model.getCompanyNameFromShortName(companiesList[position].shortName))
        view.findNavController().navigate(R.id.action_company_ticker_to_details, bundle)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.REFRESH_STOCKS_EXCHANGE_ACTION)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }
}