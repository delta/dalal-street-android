package org.pragyan.dalal18.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.AddPhoneRequest
import dalalstreet.api.actions.AddPhoneResponse
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.fragment.OTPVerificationDialogFragment
import org.pragyan.dalal18.utils.ConnectionUtils
import javax.inject.Inject

class VerifyPhoneActivity: AppCompatActivity() {

    @Inject
    lateinit var channel: ManagedChannel

    lateinit var verifyButton: Button
    lateinit var  mobNoEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_phone_no_verify)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        verifyButton = findViewById(R.id.btnVerify)
        mobNoEditText = findViewById(R.id.etMobNo)

        verifyButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(mobNoEditText.text.toString() != "" && mobNoEditText.text.toString().length==10)
                onVerifyButtonClicked(mobNoEditText.text.toString())
                else
                    toast("Enter valid mobile number.")
            }

        })
    }

    private fun onVerifyButtonClicked(phone: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@VerifyPhoneActivity) }) {

                val phoneRequest = AddPhoneRequest
                        .newBuilder()
                        .setPhoneNumber(phone)
                        .build()

                val phoneResponse = withContext(Dispatchers.IO) { DalalActionServiceGrpc.newBlockingStub(channel).addPhone(phoneRequest) }

                toast(phoneResponse.statusMessage)

                if (phoneResponse.statusCode == AddPhoneResponse.StatusCode.OK) {
                    // sms sent.
                    val dialog = OTPVerificationDialogFragment.newInstance(phone)
                    dialog.show(supportFragmentManager, "otp_dialog")

                } else {
                    // report error.
                    toast("Server Error.")
                }
            } else {
                toast("Server Unreachable.")
            }
    }
}