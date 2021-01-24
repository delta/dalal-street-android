package org.pragyan.dalal18.fragment.mortgage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.RetrieveMortgageStocksRequest
import dalalstreet.api.actions.RetrieveMortgageStocksResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.RetrieveRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.MortgageDetails
import org.pragyan.dalal18.databinding.FragmentRetrieveBinding
import org.pragyan.dalal18.utils.*
import javax.inject.Inject

class RetrieveFragment : Fragment(), RetrieveRecyclerAdapter.OnRetrieveButtonClickListener {

    private var binding by viewLifecycle<FragmentRetrieveBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private lateinit var model: DalalViewModel

    private val mortgageDetailsList = mutableListOf<MortgageDetails>()
    private lateinit var retrieveAdapter: RetrieveRecyclerAdapter

    private val refreshMortgageListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_STOCKS_FOR_MORTGAGE, ignoreCase = true)) {
                updateMortgageDetailsListFromViewModel()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRetrieveBinding.inflate(inflater, container, false)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        retrieveAdapter = RetrieveRecyclerAdapter(context, null, this)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage)

        binding.apply {
            retrieveRecyclerView.layoutManager = LinearLayoutManager(context)
            retrieveRecyclerView.adapter = retrieveAdapter
            retrieveRecyclerView.setHasFixedSize(false)

            val tempString = "(Retrieve Rate: ${Constants.MORTGAGE_RETRIEVE_RATE}%)"
            retrieveRateTextView.text = tempString
        }

        updateMortgageDetailsListFromViewModel()
    }

    private fun updateMortgageDetailsListFromViewModel() {
        binding.retrieveRecyclerViewParentLayout.visibility = View.GONE

        mortgageDetailsList.clear()
        for ((pair, quantity) in model.mortgageStockDetails) {
            mortgageDetailsList.add(MortgageDetails(pair.first, model.getShortNameFromStockId(pair.first),
                    model.getCompanyNameFromStockId(pair.first), quantity, pair.second))
        }

        if (mortgageDetailsList.size == 0) {
            flipVisibilities(true)
        } else {
            retrieveAdapter.swapData(mortgageDetailsList)
            flipVisibilities(false)
        }
    }

    override fun onRetrieveButtonClick(position: Int, retrieveQuantity: String, stocksQuantity: String) {
        when {
            retrieveQuantity.isEmpty() || retrieveQuantity == "" -> context?.toast("Enter stocks to retrieve")
            retrieveQuantity.toInt() == 0 -> context?.toast("Enter valid number of stocks")
            retrieveQuantity.toLong() > stocksQuantity.toLong() -> context?.toast("Insufficient stocks to retrieve")
            else -> {
                GlobalScope.async (Dispatchers.Default){
                    if (ConnectionUtils.getConnectionInfo(context)) {
                        if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                            val retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                                    RetrieveMortgageStocksRequest.newBuilder().setStockId(mortgageDetailsList[position].stockId)
                                            .setStockQuantity(retrieveQuantity.toLong()).setRetrievePrice(mortgageDetailsList[position].mortgagePrice).build())
                            withContext(Dispatchers.Main) {
                                if (retrieveStocksResponse.statusCode == RetrieveMortgageStocksResponse.StatusCode.OK)
                                    context?.toast("Transaction successful")
                                else
                                    context?.toast(retrieveStocksResponse.statusMessage)

                                view?.hideKeyboard()
                            }
                        } else {
                            withContext(Dispatchers.Main) { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.main_mortgage_dest) }
                        }
                    } else {
                        withContext(Dispatchers.Main) { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.main_mortgage_dest) }
                    }
                }
            }
        }
    }

    private fun flipVisibilities(noStocksMortgaged: Boolean) {
        binding.apply {
            messageStocksMortgagedTextView.text = getString(R.string.no_stocks_mortgaged)
            messageStocksMortgagedTextView.visibility = if (noStocksMortgaged) View.VISIBLE else View.GONE
            retrieveRecyclerViewParentLayout.visibility = if (noStocksMortgaged) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.REFRESH_STOCKS_FOR_MORTGAGE)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshMortgageListReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshMortgageListReceiver)
    }
}
