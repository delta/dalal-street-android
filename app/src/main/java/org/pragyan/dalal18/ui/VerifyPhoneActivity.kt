package org.pragyan.dalal18.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.AddPhoneRequest
import dalalstreet.api.actions.AddPhoneResponse
import dalalstreet.api.actions.LogoutRequest
import dalalstreet.api.actions.LogoutResponse
import dalalstreet.api.datastreams.UnsubscribeRequest
import io.grpc.ManagedChannel
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
import org.pragyan.dalal18.dagger.SharedPreferencesModule
import org.pragyan.dalal18.dagger.StubModule
import org.pragyan.dalal18.fragment.OTPVerificationDialogFragment
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import javax.inject.Inject

class VerifyPhoneActivity: AppCompatActivity(), ConnectionUtils.OnNetworkDownHandler {

    @Inject
    lateinit var stub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    lateinit var verifyButton: Button
    lateinit var  mobNoEditText: EditText
    lateinit var logoutButton: Button
    private var logoutDialog: AlertDialog? = null
    private var errorDialog: AlertDialog? = null
    private lateinit var loadingDialog: android.app.AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        verifyButton = findViewById(R.id.btnVerify)
        mobNoEditText = findViewById(R.id.etMobNo)
        logoutButton = findViewById(R.id.btnLogoutVerifyActivity)

        logoutButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                logoutClicked()
            }

        })

        verifyButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                /*val dialog = OTPVerificationDialogFragment.newInstance("7338798208")
                dialog.arguments = intent.extras
                dialog.show(supportFragmentManager, "otp_dialog")*/
                if(mobNoEditText.text.toString() != "" )//&& mobNoEditText.text.toString().length==10)
                onVerifyButtonClicked(mobNoEditText.text.toString())
                else
                    toast("Enter valid mobile number.")
            }

        })
    }

    private fun logoutClicked() {
        val logOutBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)

        logOutBuilder
                .setMessage("Do you want to logout?")
                .setPositiveButton(getString(R.string.logout)) { _, _ -> logout() }
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
                .setTitle("Confirm Logout")
                .setCancelable(true)
                .show()
        logoutDialog = logOutBuilder.create()
    }

    fun logout() {

        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@VerifyPhoneActivity)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val logoutResponse = stub.logout(LogoutRequest.newBuilder().build())

                    uiThread {
                        if (logoutResponse.statusCode == LogoutResponse.StatusCode.OK) {

                            val stopNotificationIntent = Intent(Constants.STOP_NOTIFICATION_ACTION)
                            LocalBroadcastManager.getInstance(this@VerifyPhoneActivity).sendBroadcast(stopNotificationIntent)

                            preferences.edit().putString(Constants.EMAIL_KEY, null).putString(Constants.PASSWORD_KEY, null).putString(Constants.SESSION_KEY, null).apply()
                        }

                        startActivity(Intent(this@VerifyPhoneActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    uiThread { onNetworkDownError(resources.getString(R.string.error_server_down), R.id.home_dest) }
                }
            } else {
                uiThread { onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.home_dest) }
            }
        }
    }

    override fun onNetworkDownError(message: String?, fragment: Int) {

        errorDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage(message)
                .setPositiveButton(getString(R.string.retry), null)
                .setTitle(getString(R.string.error))
                .setCancelable(false)
                .create()

        errorDialog?.setOnShowListener {
            val positiveButton = errorDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener {
                logoutClicked()
            }
        }
        errorDialog?.show()
        contentView?.hideKeyboard()
    }

    private fun onVerifyButtonClicked(phone: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@VerifyPhoneActivity) }) {

            val dialogBox = LayoutInflater.from(this@VerifyPhoneActivity).inflate(R.layout.progress_dialog, null)
            dialogBox.findViewById<TextView>(R.id.progressDialog_textView).setText("Sending OTP...")
            loadingDialog = AlertDialog.Builder(this@VerifyPhoneActivity).setView(dialogBox).setCancelable(false).create()
            loadingDialog.show()

            val phoneRequest = AddPhoneRequest
                    .newBuilder()
                    .setPhoneNumber(phone)
                    .build()

            val phoneResponse = withContext(Dispatchers.IO) { stub.addPhone(phoneRequest) }

            toast(phoneResponse.statusMessage)

            if (phoneResponse.statusCode == AddPhoneResponse.StatusCode.OK) {
                // sms sent.
                val dialog = OTPVerificationDialogFragment.newInstance(phone)
                dialog.arguments = intent.extras
                dialog.show(supportFragmentManager, "otp_dialog")
                loadingDialog.dismiss()
            }
        } else {
            toast("Server Unreachable.")
        }
    }
}