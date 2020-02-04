package org.pragyan.dalal18.fragment.smsVerification

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.VerifyOTPRequest
import dalalstreet.api.actions.VerifyOTPResponse
import kotlinx.android.synthetic.main.fragment_otp_verification.*
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

class OTPVerificationFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var loadingDialog: AlertDialog
    private lateinit var smsVerificationHandler: ConnectionUtils.SmsVerificationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        val dialogBox = LayoutInflater.from(activity).inflate(R.layout.progress_dialog, null)
        dialogBox.findViewById<TextView>(R.id.progressDialog_textView).text = getString(R.string.verifying_otp)
        loadingDialog = AlertDialog.Builder(activity).setView(dialogBox).setCancelable(false).create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyOtpButton.setOnClickListener { onVerifyButtonClick() }

        otpSpecialEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (charSequence?.length == 4) {
                    onVerifyButtonClick()
                }
            }

        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            smsVerificationHandler = context as ConnectionUtils.SmsVerificationHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    private fun onVerifyButtonClick() {
        if (otpSpecialEditText.text.toString().length != 4)
            context?.toast("Enter valid OTP")
        else
            checkIfOtpIsCorrect(otpSpecialEditText.text.toString(), smsVerificationHandler.phoneNumber)
    }

    private fun checkIfOtpIsCorrect(otp: String, phoneNumber: String) = lifecycleScope.launch {

        loadingDialog.show()
        view?.hideKeyboard()

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {

            val verifyOTPRequest = VerifyOTPRequest.newBuilder().setOtp(Integer.parseInt(otp)).setPhone(phoneNumber).build()

            val verifyOTPResponse = withContext(Dispatchers.IO) { actionServiceBlockingStub.verifyPhone(verifyOTPRequest) }
            loadingDialog.dismiss()

            context?.toast(verifyOTPResponse.statusMessage)

            if (verifyOTPResponse.statusCode == VerifyOTPResponse.StatusCode.OK) {
                smsVerificationHandler.phoneVerificationSuccessful()
            } else if (verifyOTPResponse.statusCode == VerifyOTPResponse.StatusCode.OTPExpiredError) {
                smsVerificationHandler.navigateToAddPhone()
            }

        } else {
            smsVerificationHandler.onNetworkDownError(resources.getString(R.string.error_check_internet))
        }
    }

    fun adjustUnits(left: Long): String {
        return if(left < 10) "0$left"
        else left.toString()
    }

    override fun onResume() {
        super.onResume()

        phoneNumberTextView.text = smsVerificationHandler.phoneNumber

        if (smsVerificationHandler.phoneNumber.isBlank() || smsVerificationHandler.phoneNumber.isEmpty())
            smsVerificationHandler.navigateToAddPhone()

        object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                resendOtpButton.text = this@OTPVerificationFragment.getString(R.string.resend_otp)
                resendOtpButton.isEnabled = true
            }

            override fun onTick(left: Long) {
                val resendButtonText = getString(R.string.resend_in_00) + adjustUnits(left/1000)
                resendOtpButton.text = resendButtonText
            }

        }.start()

        resendOtpButton.setOnClickListener {
            smsVerificationHandler.navigateToAddPhone()
        }
    }
}
