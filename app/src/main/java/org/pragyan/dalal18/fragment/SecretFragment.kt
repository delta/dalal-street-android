package org.pragyan.dalal18.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.*
import kotlinx.android.synthetic.main.fragment_secret.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import javax.inject.Inject

/**
 * Admin Panel: No sanity checks for UI have been implemented
 */

class SecretFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_secret, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.admin_panel)

        openMarketButton.setOnClickListener { openMarket() }
        closeMarketButton.setOnClickListener { closeMarket() }
        sendNotificationButton.setOnClickListener { sendNotification() }
        setBankruptButton.setOnClickListener { setCompanyBankrupt(bankruptSwitch.isChecked) }
        setDividendButton.setOnClickListener { setCompanyDividends(dividendSwitch.isChecked) }
        stocksToExchangeButton.setOnClickListener { addStocksToExchange() }
        updateStockPriceButton.setOnClickListener { updateStockPrice() }
        sendNewsButton.setOnClickListener { sendNews() }
    }

    private fun openMarket() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                actionServiceBlockingStub.openMarket(OpenMarket.OpenMarketRequest.newBuilder().setUpdateDayHighAndLow(true).build())
            }
        }
    }

    private fun closeMarket() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                actionServiceBlockingStub.closeMarket(CloseMarket.CloseMarketRequest.newBuilder().setUpdatePrevDayClose(true).build())
            }
        }
    }

    private fun sendNotification() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (userIdEditText.text.toString().isNotBlank() && notificationEditText.text.toString().isNotBlank())
                    actionServiceBlockingStub.sendNotifications(SendNotifications.SendNotificationsRequest.newBuilder()
                            .setText(notificationEditText.text.toString())
                            .setUserId(userIdEditText.text.toString().toInt())
                            .setIsGlobal(isGlobalSwitch.isChecked)
                            .build())
            }
        }
    }

    private fun setCompanyBankrupt(isBankrupt: Boolean) = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (stockIdEditText2.text.toString().isNotBlank())
                    actionServiceBlockingStub.setBankruptcy(SetBankruptcyRequest.newBuilder()
                            .setStockId(stockIdEditText2.text.toString().toInt())
                            .setIsBankrupt(isBankrupt)
                            .build())
            }
        }
    }

    private fun setCompanyDividends(givesDividends: Boolean) = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (stockIdEditText2.text.toString().isNotBlank())
                    actionServiceBlockingStub.setGivesDividends(SetGivesDividendsRequest.newBuilder()
                            .setStockId(stockIdEditText2.text.toString().toInt())
                            .setGivesDividends(givesDividends)
                            .build())
            }
        }
    }

    private fun addStocksToExchange() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (stockIdEditText.text.toString().isNotBlank() && stocksToExchangeEditText.text.toString().isNotBlank())
                    actionServiceBlockingStub.addStocksToExchange(AddStocksToExchange.AddStocksToExchangeRequest.newBuilder()
                            .setStockId(stockIdEditText.text.toString().toInt())
                            .setNewStocks(stocksToExchangeEditText.text.toString().toLong())
                            .build())
            }
        }
    }

    private fun updateStockPrice() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (stockIdEditText.text.toString().isNotBlank() && newStockPriceEditText.text.toString().isNotBlank())
                    actionServiceBlockingStub.updateStockPrice(UpdateStockPrice.UpdateStockPriceRequest.newBuilder()
                            .setStockId(stockIdEditText.text.toString().toInt())
                            .setNewPrice(newStockPriceEditText.text.toString().toLong())
                            .build())
            }
        }
    }

    private fun sendNews() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if (headlinesEditText.text.toString().isNotBlank() && newsDescriptionEditText.text.toString().isNotBlank() &&
                        newsImageUrlEditText.text.toString().isNotBlank()) {
                    actionServiceBlockingStub.addMarketEvent(AddMarketEvent.AddMarketEventRequest.newBuilder()
                            .setHeadline(headlinesEditText.text.toString())
                            .setText(newsDescriptionEditText.text.toString())
                            .setStockId(stockIdEditText3.text.toString().toInt())
                            .setImageUrl(newsImageUrlEditText.text.toString())
                            .setIsGlobal(isGlobalNewsSwitch.isChecked)
                            .build())
                }
            }
        }
    }
}

/**
 * Open Market
 * Close Market
 *
 * AddStocksToExchange
 * AddMarketEvent
 * UpdateStockPrice
 *
 * SetBankruptcy
 * SetGivesDividend
 *
 * SendNotification
 */