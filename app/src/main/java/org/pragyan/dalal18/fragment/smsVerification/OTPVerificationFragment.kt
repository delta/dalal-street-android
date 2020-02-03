package org.pragyan.dalal18.fragment.smsVerification

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.VerifyOTPRequest
import dalalstreet.api.actions.VerifyOTPResponse
import io.grpc.ManagedChannel
import kotlinx.android.synthetic.main.layout_otp_verification_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import javax.inject.Inject

class OTPVerificationFragment : Fragment() {

    @Inject
    lateinit var channel: ManagedChannel

    @Inject
    lateinit var stub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    var mobNumber = ""
    private lateinit var loadingDialog: AlertDialog
    lateinit var smsVerificationHandler: ConnectionUtils.SmsVerificationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        val dialogBox = LayoutInflater.from(activity).inflate(R.layout.progress_dialog, null)
        dialogBox.findViewById<TextView>(R.id.progressDialog_textView).text = "Sending OTP..."
        loadingDialog = AlertDialog.Builder(activity).setView(dialogBox).setCancelable(false).create()
        loadingDialog.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_otp_verification_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneNumberEditText.setText(mobNumber)

        resendOtpButton.setOnClickListener {
            sendOtpAgain()
            resendOtpButton.visibility = INVISIBLE
            Handler().postDelayed({
                resendOtpButton.visibility = VISIBLE
            }, 60 * 1000)
        }
        verifyOtpButton.setOnClickListener {
            if (otpSpecialEditText.text.toString() == "")
                Toast.makeText(context, "Enter OTP.", Toast.LENGTH_SHORT).show()
            else
                checkIfOtpIsCorrect(otpSpecialEditText.text.toString())
        }
        loadingDialog.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            smsVerificationHandler = context as ConnectionUtils.SmsVerificationHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    private fun checkIfOtpIsCorrect(OTP: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {

            val verifyOTPRequest = VerifyOTPRequest
                    .newBuilder()
                    .setOtp(Integer.parseInt(OTP))
                    .setPhone(mobNumber)
                    .build()

            val verifyOTPResponse = withContext(Dispatchers.IO) { stub.verifyPhone(verifyOTPRequest) }
            Toast.makeText(context, verifyOTPResponse.statusMessage, Toast.LENGTH_SHORT).show()

            if (verifyOTPResponse.statusCode == VerifyOTPResponse.StatusCode.OK) {
                // go to main with all intent values
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        } else {
            smsVerificationHandler.onNetworkDownError(resources.getString(R.string.error_check_internet))
        }
    }

    private fun sendOtpAgain() {
        // will go back to last activity and when button clicked, a new otp will be sent.
        //dismiss()
    }

}