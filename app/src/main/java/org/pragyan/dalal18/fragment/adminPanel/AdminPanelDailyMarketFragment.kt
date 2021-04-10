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
import kotlinx.android.synthetic.main.fragment_adminpanel_dailymarket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentAdminpanelDailymarketBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

class AdminPanelDailyMarketFragment : Fragment() {
    private var binding by viewLifecycle<FragmentAdminpanelDailymarketBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel
    private var message = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAdminpanelDailymarketBinding.inflate(inflater, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            openMarketButton.setOnClickListener { openMarket() }
            closeMarketButton.setOnClickListener { closeMarket() }
            updateEndOfDayValue.setOnClickListener { updateEndOfDayValues() }
            addDailyChallengeButton.setOnClickListener { addDailyChallenge() }
            openDailyChallengeButton.setOnClickListener { openDailyChallenge() }
            closeDailyChallengeButton.setOnClickListener { closeDailyChallenge() }
            setMarketDayButton.setOnClickListener { setMarketDay() }
        }

    }

        private fun openMarket() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val response = actionServiceBlockingStub.openMarket(OpenMarket.OpenMarketRequest.newBuilder().setUpdateDayHighAndLow(true).build())
                message = response.statusMessage
            }
        }
        context?.toast(message)
    }
    private fun openDailyChallenge() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val response = actionServiceBlockingStub.openDailyChallenge(OpenDailyChallenge.OpenDailyChallengeRequest.newBuilder().build())
                message = response.statusMessage
            }
        }
        context?.toast(message)
    }
    private fun closeDailyChallenge() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val response = actionServiceBlockingStub.closeDailyChallenge(CloseDailyChallenge.CloseDailyChallengeRequest.newBuilder().build())
                message = response.statusMessage
            }
        }
        context?.toast(message)
    }

        private fun closeMarket() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val response = actionServiceBlockingStub.closeMarket(CloseMarket.CloseMarketRequest.newBuilder().setUpdatePrevDayClose(true).build())
                message = response.statusMessage
            }
        }
        context?.toast(message)
    }

    private fun updateEndOfDayValues() = lifecycleScope.launch {
        view?.hideKeyboard()
        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val response = actionServiceBlockingStub.updateEndOfDayValues(UpdateEndOfDayValues.UpdateEndOfDayValuesRequest.getDefaultInstance())
                message = response.statusMessage
            }
        }
        context?.toast(message)
    }
    private fun setMarketDay() = lifecycleScope.launch {
        view?.hideKeyboard()
        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                if(enterMarketDayEditText.text.toString().isNotBlank()){
                    val response = actionServiceBlockingStub.setMarketDay(SetMarketDay.SetMarketDayRequest.newBuilder()
                            .setMarketDay(enterMarketDayEditText.text.toString().toInt())
                            .build())
                    message = response.statusMessage
                }

            }
        }
        context?.toast(message)
    }

    private fun addDailyChallenge() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                binding.apply {
                    if (enterMarketDayEditText.text.toString().isNotBlank()  &&
                            enterValueEditextForDailyChallenge.text.toString().isNotBlank()  &&
                            rewardEditText.text.toString().isNotBlank()
                    ) {
                        val challengetype = getChallengeTypeEnum(typeOfChallengeEditText.selectedItem.toString())
                        if(challengetype == AddDailyChallenge.ChallengeType.SpecificStock){
                            if(stockIdEditTextForDailyChallenge.text.toString().isNotBlank() && enterMarketDayEditTextForAddDailyChallenge.text.toString().isNotBlank()){
                                val response = actionServiceBlockingStub.addDailyChallenge(AddDailyChallenge.AddDailyChallengeRequest.newBuilder()
                                        .setMarketDay(enterMarketDayEditTextForAddDailyChallenge.text.toString().toInt())
                                        .setChallengeType(challengetype)
                                        .setStockId(stockIdEditTextForDailyChallenge.text.toString().toInt())
                                        .setValue(enterValueEditextForDailyChallenge.text.toString().toLong())
                                        .setReward(rewardEditText.text.toString().toInt())
                                        .build())
                                message = response.statusMessage
                            }

                        }
                        else{
                            val response = actionServiceBlockingStub.addDailyChallenge(AddDailyChallenge.AddDailyChallengeRequest.newBuilder()
                                    .setMarketDay(enterMarketDayEditText.text.toString().toInt())
                                    .setChallengeType(challengetype)
                                    .setValue(enterValueEditextForDailyChallenge.text.toString().toLong())
                                    .setReward(rewardEditText.text.toString().toInt())
                                    .build())
                            message = response.statusMessage
                        }


                    }
                }
            }
        }
        context?.toast(message)
    }

    private fun getChallengeTypeEnum(type :String): AddDailyChallenge.ChallengeType {
        return when(type){
            "CASH" -> {
                AddDailyChallenge.ChallengeType.Cash
            }
            "NetWorth" ->{
                AddDailyChallenge.ChallengeType.NetWorth
            }
            "StockWorth" -> {
                AddDailyChallenge.ChallengeType.StockWorth
            }
            else -> {
                AddDailyChallenge.ChallengeType.SpecificStock
            }
        }
    }

}