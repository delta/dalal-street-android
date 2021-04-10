package org.pragyan.dalal18.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.RegisterRequest
import dalalstreet.api.actions.RegisterResponse
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.databinding.ActivityRegistrationBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

class RegistrationActivity : AppCompatActivity() {

    private val binding by viewLifecycle(ActivityRegistrationBinding::inflate)

    /* Not injecting stub directly into this context to prevent empty/null metadata attached to stub since user has not logged in. */
    @Inject
    lateinit var channel: ManagedChannel

    private val invalidReferralCodeMessage = "Invalid Referral Code"
    private var registrationAlertDialog: AlertDialog? = null
    private var invalidReferralAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.registering)
        registrationAlertDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        setSupportActionBar(binding.registrationToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.clear_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val invalidReferralAlertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        invalidReferralAlertDialogBuilder.setTitle(invalidReferralCodeMessage)
                .setMessage("Your referral code is incorrect, recheck it or keep it empty.")
                .setPositiveButton("OK") { p0, p1 ->
                    Log.i("Invalid Referral Code", "Cliced OK.")
                    invalidReferralAlertDialog?.dismiss()
                }


        invalidReferralAlertDialog = invalidReferralAlertDialogBuilder.create()

        title = "One-Time Registration"

        val countries = resources.getStringArray(R.array.countries_array)
        val arrayAdapter = ArrayAdapter(this, R.layout.company_spinner_item, countries)
        binding.apply {
            countrySpinner.adapter = arrayAdapter
            countrySpinner.setSelection(98)

            registerButton.setOnClickListener { startRegistration() }
        }
    }

    private fun startRegistration() {
        binding.apply {
            when {
                nameEditText.text.toString().isEmpty() || nameEditText.text.toString() == "" -> toast("Please enter your full name")
                passwordEditText.text.toString().length < 6 -> toast("Password must be at least 6 characters")
                passwordEditText.text.toString() != confirmPasswordEditText.text.toString() -> toast("Confirm password mismatch")
                emailEditText.text.toString().isEmpty() || emailEditText.text.toString() == "" -> toast("Please enter valid email ID")
                else -> registerAsynchronously()
            }
        }
    }

    private fun registerAsynchronously() {

        registrationAlertDialog?.show()

        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@RegistrationActivity)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val stub = DalalActionServiceGrpc.newBlockingStub(channel)

                    val response: RegisterResponse
                    binding.apply {
                        response = stub.register(RegisterRequest.newBuilder()
                                .setCountry(countrySpinner.selectedItem.toString())
                                .setEmail(emailEditText.text.toString())
                                .setFullName(nameEditText.text.toString())
                                .setPassword(passwordEditText.text.toString())
                                .setUserName(nameEditText.text.toString())
                                .setReferralCode(referralCodeEditText.text.toString())
                                .build())
                    }

                    val message = when (response.statusCode) {
                        RegisterResponse.StatusCode.OK -> "Successfully Registered! Please check your inbox to verify email."
                        RegisterResponse.StatusCode.AlreadyRegisteredError -> "You have already registered."
                        RegisterResponse.StatusCode.InvalidReferralCodeError -> invalidReferralCodeMessage
                        RegisterResponse.StatusCode.InternalServerError -> "Something went wrong"
                        else -> "Something went wrong, please try again."
                    }

                    uiThread {
                        if(message == invalidReferralCodeMessage) {
                            invalidReferralAlertDialog?.show()
                        } else {
                            val loginIntent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                            loginIntent.putExtra(REGISTER_MESSAGE_KEY, message)
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(loginIntent)
                            finish()
                        }
                    }
                } else {
                    uiThread { showErrorSnackBar(resources.getString(R.string.error_server_down)) }
                }
            } else {
                uiThread { showErrorSnackBar(resources.getString(R.string.error_check_internet)) }
            }
            uiThread { registrationAlertDialog?.dismiss() }
        }
    }

    private fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { startRegistration() }

        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }

    companion object {
        const val REGISTER_MESSAGE_KEY = "register-message-key"
    }

}