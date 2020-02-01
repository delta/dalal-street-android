package org.pragyan.dalal18.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.LoginRequest
import dalalstreet.api.actions.LoginResponse
import dalalstreet.api.actions.VerifyOTPRequest
import dalalstreet.api.actions.VerifyOTPResponse
import io.grpc.ManagedChannel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.contentView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.GlobalStockDetails
import org.pragyan.dalal18.data.StockDetails
import org.pragyan.dalal18.ui.LoginActivity
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.ui.OtpEditText
import org.pragyan.dalal18.ui.VerifyPhoneActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
import org.pragyan.dalal18.utils.hideKeyboard
import java.util.ArrayList
import javax.inject.Inject

class OTPVerificationDialogFragment : DialogFragment() {

    @Inject
    lateinit var channel: ManagedChannel

    lateinit var mobNumber: String
    lateinit var phoneNumberEditText: EditText
    lateinit var resendOtp: Button
    lateinit var verifyOtp: Button
    lateinit var otpEditText: OtpEditText

    companion object {
        fun newInstance(phNum: String) : OTPVerificationDialogFragment  {

            val f = OTPVerificationDialogFragment()
            f.mobNumber = phNum

            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_otp_verification_dialog, container, false)
        phoneNumberEditText = v.findViewById(R.id.enter_otp_mobno_edit_text)
        resendOtp = v.findViewById(R.id.btnResendOtp)
        verifyOtp = v.findViewById(R.id.btnVerifyOtp)
        otpEditText = v.findViewById(R.id.et_otp)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneNumberEditText.setText(mobNumber)

        resendOtp.setOnClickListener { sendOtpAgain() }
        verifyOtp.setOnClickListener {
            if (otpEditText.text!!.toString() == "")
                Toast.makeText(context, "Enter OTP.", Toast.LENGTH_SHORT).show()
            else
                checkIfOtpIsCorrect(otpEditText.text!!.toString())
        }
    }

    private fun checkIfOtpIsCorrect(OTP: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {
            val verifyOTPRequest = VerifyOTPRequest
                    .newBuilder()
                    .setOtp(Integer.parseInt(OTP))
                    .build()

            val verifyOTPResponse = withContext(Dispatchers.IO) { DalalActionServiceGrpc.newBlockingStub(channel).verifyOTP(verifyOTPRequest) }

            Toast.makeText(context,verifyOTPResponse.statusMessage,Toast.LENGTH_SHORT).show()

            if (verifyOTPResponse.statusCode == VerifyOTPResponse.StatusCode.OK) {
                // go to main with all intent values
                val intent = Intent(activity,MainActivity::class.java)
                startActivity(intent)
                activity?.finish()

            } else {
                Toast.makeText(context, "Wrong OTP.", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(context, "Server Unreachable.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOtpAgain() {
        // will go back to last activity and when button clicked, a new otp will be sent.
        dismiss()
    }
}