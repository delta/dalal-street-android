package org.pragyan.dalal18.fragment.adminPanel

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.BlockUser
import dalalstreet.api.actions.SendNotifications
import dalalstreet.api.actions.UnblockAllUsers
import dalalstreet.api.actions.UnblockUser
import kotlinx.android.synthetic.main.fragment_adminpanel_userspecific.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentAdminpanelUserspecificBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

@Suppress("PLUGIN_WARNING")
class AdminPanelUserSpecificFragment : Fragment() {
    private var binding by viewLifecycle<FragmentAdminpanelUserspecificBinding>()
    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel
    private var message = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAdminpanelUserspecificBinding.inflate(inflater, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            blockUserButton.setOnClickListener { blockUser() }
            unblockUserButton.setOnClickListener { unblockUser() }
            unblockAllUserButton.setOnClickListener { unblockAllUser() }
            sendNotificationButton.setOnClickListener { sendNotification() }
            inspectUserButton.setOnClickListener { inspectUser() }
        }

    }

    private fun blockUser() = lifecycleScope.launch {
        view?.hideKeyboard()
        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                if (blockuserEditText.text.toString().isNotBlank()
                ) {
                    val response = actionServiceBlockingStub.blockUser(BlockUser.BlockUserRequest.newBuilder()
                            .setUserId(blockuserEditText.text.toString().toInt())
                            .build())
                    message = response.statusMessage
                }
            }
        }
        context?.toast(message)
    }
    private fun unblockUser() = lifecycleScope.launch {
        view?.hideKeyboard()
        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                if (unblockuserEditText.text.toString().isNotBlank()
                ) {
                    val response = actionServiceBlockingStub.unBlockUser(UnblockUser.UnblockUserRequest.newBuilder()
                            .setUserId(unblockuserEditText.text.toString().toInt())
                            .build())
                    message = response.statusMessage
                }
            }
        }
        context?.toast(message)
    }
    private fun unblockAllUser() = lifecycleScope.launch {
        view?.hideKeyboard()
        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val response = actionServiceBlockingStub.unBlockAllUsers(UnblockAllUsers.UnblockAllUsersRequest.getDefaultInstance())
                    message = response.statusMessage

            }
        }
        context?.toast(message)
    }
    private fun sendNotification() = lifecycleScope.launch {
        view?.hideKeyboard()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                binding.apply {
                    if (userIdforNotification.text.toString().isNotBlank() && notificationEditText.text.toString().isNotBlank()) {
                        val response = actionServiceBlockingStub.sendNotifications(SendNotifications.SendNotificationsRequest.newBuilder()
                                .setText(notificationEditText.text.toString())
                                .setUserId(userIdforNotification.text.toString().toInt())
                                .setIsGlobal(isGlobalSwitch.isChecked)
                                .build())

                        message = response.statusMessage
                    }
                }
            }
        }
        context?.toast(message)
    }

    private fun inspectUser() = lifecycleScope.launch {
        view?.hideKeyboard()
        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                binding.apply {
                    if (userIdforInspectUser.text.toString().isNotBlank() && noOfDaysForInspectUser.text.toString().isNotBlank()) {
                        val response = actionServiceBlockingStub.inspectUser(dalalstreet.api.actions.InspectUserRequest.newBuilder()
                                .setDay(noOfDaysForInspectUser.text.toString().toInt())
                                .setUserId(userIdforInspectUser.text.toString().toInt())
                                .setTransactionType(askUserSwitch.isChecked)
                                .build())
                        val rep = response.listList
                       rep.forEach{
                            d("SPD","in response")
                            context?.longToast("User Email : ${it.email} \n User Id : ${it.id} \n Position : ${it.position} \n StockSum : ${it.stockSum} \n TransactionCount : ${it.transactionCount}")
//                            inspectUser.email = it.email
//                            inspectUser.id = it.id
//                            inspectUser.position = it.position
//                            inspectUser.stockSum = it.stockSum
//                            inspectUser.transactionCount = it.transactionCount

                      }
                    }
                }
            }
        }
       // context?.longToast("User Email : ${inspectUser.email} \n User Id : ${inspectUser.id} \n Position : ${inspectUser.position} \n StockSum : ${inspectUser.stockSum} \n TransactionCount : ${inspectUser.transactionCount}")
    }


}
