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
import kotlinx.android.synthetic.main.fragment_adminpanel_userspecific.*
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
import org.pragyan.dalal18.databinding.FragmentAdminpanelUserspecificBinding
import org.pragyan.dalal18.databinding.FragmentSecretBinding
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


}
