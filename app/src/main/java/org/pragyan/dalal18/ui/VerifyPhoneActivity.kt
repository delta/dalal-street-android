package org.pragyan.dalal18.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.AddPhoneRequest
import dalalstreet.api.actions.AddPhoneResponse
import dalalstreet.api.actions.ForgotPasswordResponse
import io.grpc.ManagedChannel
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.fragment.OTPVerficationDialogFragment
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
                onVerifyButtonClicked()
            }

        })
    }

    private fun onVerifyButtonClicked() {
        var phone: String? = null

        if(mobNoEditText.text.toString()!="") {
            phone = mobNoEditText.text.toString()

            val request = AddPhoneRequest
                    .newBuilder()
                    .setPhoneNumber(phone)
                    .build()

            val phoneResponse = DalalActionServiceGrpc.newBlockingStub(channel).addPhone(request)

            if (phoneResponse.statusCode == AddPhoneResponse.StatusCode.OK) {
                // send sms here.

                val dialog = OTPVerficationDialogFragment.newInstance(phone)
                dialog.show(supportFragmentManager,"otp_dialog")

            }
            else {
                // report server error.
            }
        }
        else {
            Toast.makeText(applicationContext,"Enter valid mobile number.",Toast.LENGTH_SHORT).show()
        }
    }

}