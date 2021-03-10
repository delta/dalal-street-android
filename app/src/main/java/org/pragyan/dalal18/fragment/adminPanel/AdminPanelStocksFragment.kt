package org.pragyan.dalal18.fragment.adminPanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.*
import kotlinx.android.synthetic.main.fragment_getting_started.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentAdminpanelStocksBinding
import org.pragyan.dalal18.databinding.FragmentSecretBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

@Suppress("PLUGIN_WARNING")
class AdminPanelStocksFragment : Fragment() {
    private var binding by viewLifecycle<FragmentAdminpanelStocksBinding>()
    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel
    private var message = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAdminpanelStocksBinding.inflate(inflater, container, false)
        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setBankruptButton.setOnClickListener { setCompanyBankrupt(bankruptSwitch.isChecked) }
            setDividendButton.setOnClickListener { setCompanyDividends(dividendSwitch.isChecked) }
            stocksToExchangeButton.setOnClickListener { addStocksToExchange() }
            updateStockPriceButton.setOnClickListener { updateStockPrice() }
            sendNewsButton.setOnClickListener { sendNews() }
        }

    }

    private fun setCompanyBankrupt(isBankrupt: Boolean) = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (binding.stockIdEditText2.text.toString().isNotBlank()) {
                    val response = actionServiceBlockingStub.setBankruptcy(SetBankruptcyRequest.newBuilder()
                            .setStockId(binding.stockIdEditText2.text.toString().toInt())
                            .setIsBankrupt(isBankrupt)
                            .build())
                    message = response.statusMessage
                }
            }
        }
        context?.toast(message)
    }

    private fun setCompanyDividends(givesDividends: Boolean) = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (binding.stockIdEditText2.text.toString().isNotBlank()) {
                    val response = actionServiceBlockingStub.setGivesDividends(SetGivesDividendsRequest.newBuilder()
                            .setStockId(binding.stockIdEditText2.text.toString().toInt())
                            .setGivesDividends(givesDividends)
                            .build())
                    message = response.statusMessage
                }
            }
        }
        context?.toast(message)
    }

    private fun addStocksToExchange() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (binding.stockIdEditText.text.toString().isNotBlank() && binding.stocksToExchangeEditText.text.toString().isNotBlank()) {
                    val response = actionServiceBlockingStub.addStocksToExchange(AddStocksToExchange.AddStocksToExchangeRequest.newBuilder()
                            .setStockId(binding.stockIdEditText.text.toString().toInt())
                            .setNewStocks(binding.stocksToExchangeEditText.text.toString().toLong())
                            .build())
                    message = response.statusMessage
                }
            }
        }
        context?.toast(message)
    }

    private fun updateStockPrice() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (binding.stockIdEditText.text.toString().isNotBlank() && binding.newStockPriceEditText.text.toString().isNotBlank()) {
                    val response = actionServiceBlockingStub.updateStockPrice(UpdateStockPrice.UpdateStockPriceRequest.newBuilder()
                            .setStockId(binding.stockIdEditText.text.toString().toInt())
                            .setNewPrice(binding.newStockPriceEditText.text.toString().toLong())
                            .build())
                    message = response.statusMessage
                }
            }
        }
        context?.toast(message)
    }

    private fun sendNews() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                binding.apply {
                    if (headlinesEditText.text.toString().isNotBlank() && newsDescriptionEditText.text.toString().isNotBlank() &&
                            newsImageUrlEditText.text.toString().isNotBlank()) {
                        val response = actionServiceBlockingStub.addMarketEvent(AddMarketEvent.AddMarketEventRequest.newBuilder()
                                .setStockId(stockIdEditTextForNews.text.toString().toInt())
                                .setHeadline(headlinesEditText.text.toString())
                                .setText(newsDescriptionEditText.text.toString())
                                .setImageUrl(newsImageUrlEditText.text.toString())
                                .setIsGlobal(isGlobalNewsSwitch.isChecked)
                                .build())
                        message = response.statusMessage
                    }
                }
            }
        }
        context?.toast(message)
    }

}
