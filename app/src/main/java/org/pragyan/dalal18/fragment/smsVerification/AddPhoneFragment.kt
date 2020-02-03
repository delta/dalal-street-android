package org.pragyan.dalal18.fragment.smsVerification

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.AddPhoneRequest
import dalalstreet.api.actions.AddPhoneResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.utils.ConnectionUtils
import javax.inject.Inject

class AddPhoneFragment : Fragment() {

    @Inject
    lateinit var stub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var verifyButton: Button
    lateinit var mobNoEditText: EditText
    lateinit var loadingDialog: AlertDialog
    private lateinit var smsVerificationHandler: ConnectionUtils.SmsVerificationHandler

    private lateinit var spinner: MaterialBetterSpinner
    var countryCode = "+91"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_add_phone, container, false)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        verifyButton = view.findViewById(R.id.btnVerify)
        mobNoEditText = view.findViewById(R.id.etMobNo)
        spinner = view.findViewById(R.id.spinnerCountry)

        ArrayAdapter.createFromResource(
                context!!,
                R.array.DialingCountryCode,
                R.layout.spinner_item_country
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.setAdapter(adapter)
            spinner.setTextColor(Color.parseColor("#FFFFFF"))
            spinner.setHintTextColor(Color.parseColor("#FFFFFF"))
            spinner.setText("91,IN")
        }

        spinner.setOnItemClickListener { _, _, _, _ ->
            val selection = spinner.text.toString()
            countryCode = ""
            val lastIndex = selection.indexOf(",")
            if (lastIndex != -1)
                countryCode = selection.substring(0, lastIndex)
        }

        verifyButton.setOnClickListener {
            /*val dialog = OTPVerificationDialogFragment.newInstance("7338798208")
                dialog.arguments = intent.extras
                dialog.show(supportFragmentManager, "otp_dialog")*/
            if (mobNoEditText.text.toString() != "")//&& mobNoEditText.text.toString().length==10)
                onVerifyButtonClicked(countryCode + mobNoEditText.text.toString())
            else
                context?.toast("Enter valid mobile number.")
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

    private fun onVerifyButtonClicked(phone: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {

            val dialogBox = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            dialogBox.findViewById<TextView>(R.id.progressDialog_textView).text = "Sending OTP..."
            loadingDialog = AlertDialog.Builder(context).setView(dialogBox).setCancelable(false).create()
            loadingDialog.show()

            val phoneRequest = AddPhoneRequest
                    .newBuilder()
                    .setPhoneNumber(phone)
                    .build()

            val phoneResponse = withContext(Dispatchers.IO) { stub.addPhone(phoneRequest) }

            context?.toast(phoneResponse.statusMessage)

            if (phoneResponse.statusCode == AddPhoneResponse.StatusCode.OK) {
                // TODO: Switch fragments.
                loadingDialog.dismiss()
            }
        } else {
            smsVerificationHandler.onNetworkDownError(resources.getString(R.string.error_check_internet))
        }
    }
}
