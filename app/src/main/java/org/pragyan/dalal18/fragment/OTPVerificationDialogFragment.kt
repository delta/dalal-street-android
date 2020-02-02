package org.pragyan.dalal18.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.VerifyOTPRequest
import dalalstreet.api.actions.VerifyOTPResponse
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.ui.OtpEditText
import org.pragyan.dalal18.utils.ConnectionUtils
import javax.inject.Inject

class OTPVerificationDialogFragment : DialogFragment() {

    @Inject
    lateinit var channel: ManagedChannel

    @Inject
    lateinit var stub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    lateinit var mobNumber: String
    lateinit var phoneNumberEditText: EditText
    lateinit var resendOtp: Button
    lateinit var verifyOtp: Button
    lateinit var otpEditText: OtpEditText
    lateinit var bundle: Bundle
    private lateinit var loadingDialog: AlertDialog

    companion object {
        fun newInstance(phoneNumber: String) : OTPVerificationDialogFragment  {

            val f = OTPVerificationDialogFragment()
            f.mobNumber = phoneNumber

            return f
        }
        var BUNDLE_KEY = "BUNDLE_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        bundle = this.arguments!!

        val dialogBox = LayoutInflater.from(activity).inflate(R.layout.progress_dialog, null)
        dialogBox.findViewById<TextView>(R.id.progressDialog_textView).setText("Sending OTP...")
        loadingDialog = AlertDialog.Builder(activity).setView(dialogBox).setCancelable(false).create()
        loadingDialog.show()
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


        resendOtp.setOnClickListener {
            sendOtpAgain()
            resendOtp.visibility = INVISIBLE
            Handler().postDelayed({
                resendOtp.visibility = VISIBLE
            }, 60 * 1000)
        }
        verifyOtp.setOnClickListener {
            if (otpEditText.text!!.toString() == "")
                Toast.makeText(context, "Enter OTP.", Toast.LENGTH_SHORT).show()
            else
                checkIfOtpIsCorrect(otpEditText.text!!.toString())

            /*val intent = Intent(activity,MainActivity::class.java)
            intent.putExtra(BUNDLE_KEY,bundle)
            startActivity(intent)
            activity?.finish()*/
        }
        loadingDialog.dismiss()
    }

    private fun checkIfOtpIsCorrect(OTP: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {

            System.out.println(""+Integer.parseInt(OTP)+" is the otp")

            val verifyOTPRequest = VerifyOTPRequest
                    .newBuilder()
                    .setOtp(Integer.parseInt(OTP))
                    .setPhone(mobNumber)
                    .build()

            val verifyOTPResponse = withContext(Dispatchers.IO) { stub.verifyOTP(verifyOTPRequest) }
            Toast.makeText(context,verifyOTPResponse.statusMessage,Toast.LENGTH_SHORT).show()

            if (verifyOTPResponse.statusCode == VerifyOTPResponse.StatusCode.OK) {
                // go to main with all intent values
                val intent = Intent(activity,MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
        else {
            Toast.makeText(context, "Server Unreachable.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOtpAgain() {
        // will go back to last activity and when button clicked, a new otp will be sent.
        //dismiss()
    }

}