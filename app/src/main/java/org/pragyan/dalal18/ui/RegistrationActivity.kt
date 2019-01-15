package org.pragyan.dalal18.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.Register
import dalalstreet.api.actions.Register.RegisterResponse
import io.grpc.ManagedChannel
import kotlinx.android.synthetic.main.activity_registration.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import javax.inject.Inject

class RegistrationActivity : AppCompatActivity() {

    /* Not injecting stub directly into this context to prevent empty/null metadata attached to stub since user has not logged in. */
    @Inject
    lateinit var channel: ManagedChannel

    private var registrationAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.registering)
        registrationAlertDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        setSupportActionBar(registrationToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.clear_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "One-Time Registration"

        val countries = resources.getStringArray(R.array.countries_array)
        val arrayAdapter = ArrayAdapter(this, R.layout.company_spinner_item, countries)
        countrySpinner.adapter = arrayAdapter
        countrySpinner.setSelection(98)

        registerButton.setOnClickListener { startRegistration() }
    }

    private fun startRegistration() {
        if (nameEditText.text.toString().isEmpty() || nameEditText.text.toString() == "") {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
        } else if (passwordEditText.text.toString().length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        } else if (passwordEditText.text.toString() != confirmPasswordEditText.text.toString()) {
            Toast.makeText(this, "Confirm password mismatch", Toast.LENGTH_SHORT).show()
        } else if (emailEditText.text.toString().isEmpty() || emailEditText.text.toString() == "") {
            Toast.makeText(this, "Please enter valid email ID", Toast.LENGTH_SHORT).show()
        } else {
            registerAsynchronously()
        }
    }

    private fun registerAsynchronously() {

        registrationAlertDialog?.show()

        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@RegistrationActivity) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val stub = DalalActionServiceGrpc.newBlockingStub(channel)

                val response = stub.register(Register.RegisterRequest.newBuilder()
                        .setCountry(countrySpinner.selectedItem.toString())
                        .setEmail(emailEditText.text.toString())
                        .setFullName(nameEditText.text.toString())
                        .setPassword(passwordEditText.text.toString())
                        .setUserName(nameEditText.text.toString())
                        .build())

                val message = when {
                    response.statusCode == RegisterResponse.StatusCode.OK -> "Successfully Registered !"
                    response.statusCode == RegisterResponse.StatusCode.AlreadyRegisteredError -> "You have already registered."
                    else -> "Internal server error."
                }

                uiThread {
                    val loginIntent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                    loginIntent.putExtra(REGISTER_MESSAGE_KEY, message)
                    startActivity(loginIntent)
                    finish()
                }
            } else {
                uiThread {
                    startActivity(Intent(this@RegistrationActivity, SplashActivity::class.java))
                    finish()
                }
            }
        }
    }

    companion object {
        const val REGISTER_MESSAGE_KEY = "register-message-key"
    }
}