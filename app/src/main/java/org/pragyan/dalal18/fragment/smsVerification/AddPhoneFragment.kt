package org.pragyan.dalal18.fragment.smsVerification

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.AddPhoneRequest
import dalalstreet.api.actions.AddPhoneResponse
import kotlinx.android.synthetic.main.fragment_add_phone.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.hideKeyboard
import javax.inject.Inject

class AddPhoneFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    lateinit var loadingDialog: AlertDialog
    private lateinit var smsVerificationHandler: ConnectionUtils.SmsVerificationHandler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_add_phone, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        verifyButton.setOnClickListener {
            if (extensionEditText.text.toString().isEmpty() || extensionEditText.text.toString().isBlank())
                context?.toast("Enter valid country code")
            else if (mobileNumberEditText.text.toString().isEmpty() || mobileNumberEditText.text.toString().isBlank())
                context?.toast("Enter valid mobile number")
            else
                sendAddPhoneNumberAsynchronously(extensionEditText.text.toString() + mobileNumberEditText.text.toString())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            smsVerificationHandler = context as ConnectionUtils.SmsVerificationHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    private fun sendAddPhoneNumberAsynchronously(phoneNumber: String) = lifecycleScope.launch {

        view?.hideKeyboard()

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {

            val dialogBox = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            dialogBox.findViewById<TextView>(R.id.progressDialog_textView).text = getString(R.string.sending_otp)
            loadingDialog = AlertDialog.Builder(context).setView(dialogBox).setCancelable(false).create()
            loadingDialog.show()

            val phoneRequest = AddPhoneRequest.newBuilder().setPhoneNumber(phoneNumber).build()

            val phoneResponse = withContext(Dispatchers.IO) { actionServiceBlockingStub.addPhone(phoneRequest) }

            context?.toast(phoneResponse.statusMessage)
            loadingDialog.dismiss()

            if (phoneResponse.statusCode == AddPhoneResponse.StatusCode.OK) {
                smsVerificationHandler.navigateToOtpVerification(phoneNumber)
            }
        } else {
            smsVerificationHandler.onNetworkDownError(resources.getString(R.string.error_check_internet))
        }
    }
}
