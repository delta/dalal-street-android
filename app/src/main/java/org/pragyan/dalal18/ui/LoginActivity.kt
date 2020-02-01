package org.pragyan.dalal18.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.ForgotPasswordRequest
import dalalstreet.api.actions.ForgotPasswordResponse
import dalalstreet.api.actions.LoginRequest
import dalalstreet.api.actions.LoginResponse
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
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
import org.pragyan.dalal18.dagger.DalalStreetApplicationComponent
import org.pragyan.dalal18.utils.hideKeyboard
import java.util.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var channel: ManagedChannel

    @Inject
    lateinit var preferences: SharedPreferences

    private var signingInAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val toolbar = findViewById<Toolbar>(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        title = getString(R.string.app_name)

        signingInAlertDialog = AlertDialog.Builder(this).setView(R.layout.progress_dialog).setCancelable(false).create()

        if (intent.getStringExtra(RegistrationActivity.REGISTER_MESSAGE_KEY) != null) {
            AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("Registration Message")
                    .setMessage(intent.getStringExtra(RegistrationActivity.REGISTER_MESSAGE_KEY))
                    .setPositiveButton("OKAY") { dI, _ -> dI.dismiss() }
                    .setCancelable(false)
                    .show()
        }

        clickRegisterTextView.setOnClickListener { onRegisterButtonClick() }
        play_button.setOnClickListener { onLoginButtonClick() }
        forgotPasswordTextView.setOnClickListener { onForgotPasswordClick() }

        startLoginProcess(false)
    }

    private fun startLoginProcess(startedFromServerDown: Boolean) {

        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@LoginActivity)) {
                uiThread {
                    play_button.isEnabled = true

                    if (startedFromServerDown)
                        onLoginButtonClick()
                }
            } else {
                uiThread {
                    play_button.isEnabled = false
                    showSnackBar(resources.getString(R.string.error_check_internet))
                }
            }
        }
    }

    private fun onLoginButtonClick() {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@LoginActivity)) {
                uiThread {
                    if (validateEmail(emailEditText) && validatePassword()) {
                        val email = emailEditText.text.toString()
                        val password = passwordEditText.text.toString()
                        signingInAlertDialog?.show()
                        loginAsynchronously(email, password)
                    }
                }
            } else {
                uiThread { startLoginProcess(false) }
            }
        }
    }

    private fun onRegisterButtonClick() {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }

    private fun onForgotPasswordClick() {
        val changePasswordView = LayoutInflater.from(this@LoginActivity).inflate(R.layout.change_password, null);
        val emailEditText = changePasswordView.findViewById(R.id.changePasswordEmailEditText) as EditText

        val changePasswordDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setView(changePasswordView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    emailEditText.hideKeyboard()
                    dialog.dismiss()
                }
                .setCancelable(false)
                .setPositiveButton("Reset") { dI, _ ->
                    if (emailEditText.text.toString().isEmpty() || emailEditText.text.toString().isBlank() || !validateEmail(emailEditText)) {
                        toast("Enter a valid Email ID")
                        onForgotPasswordClick()
                    } else {
                        sendForgotPasswordRequestAsynchronously(emailEditText.text.toString())
                        dI.dismiss()
                    }
                }
                .create()

        changePasswordDialog.show()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        emailEditText.requestFocus()
    }

    private fun validateEmail(emailEditText: EditText): Boolean {

        if (emailEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return false
        } else if (!emailEditText.text.toString().contains("@")) {
            emailEditText.error = "Enter a valid email"
            emailEditText.requestFocus()
            return false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        if (passwordEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            passwordEditText.error = "Enter password"
            passwordEditText.requestFocus()
            return false
        }
        return true
    }

    private fun loginAsynchronously(email: String, password: String) {

        val loginRequest = LoginRequest
                .newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build()

        val stub = DalalActionServiceGrpc.newBlockingStub(channel)

        doAsync {

            if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val loginResponse = stub.login(loginRequest)

                uiThread {

                    signingInAlertDialog?.dismiss()

                    if (loginResponse.statusCode == LoginResponse.StatusCode.OK) {

                        MiscellaneousUtils.sessionId = loginResponse.sessionId

                        if (passwordEditText.text.toString() != "" || passwordEditText.text.toString().isNotEmpty())
                            preferences.edit()
                                    .putString(Constants.EMAIL_KEY, loginResponse.user.email)
                                    .putString(Constants.PASSWORD_KEY, passwordEditText.text.toString())
                                    .putString(Constants.SESSION_KEY, loginResponse.sessionId)
                                    .apply()

                        // Adding user's stock details
                        val stocksOwnedList = ArrayList<StockDetails>(30)
                        val stocksOwnedMap = loginResponse.stocksOwnedMap.orEmpty()

                        for (i in 1..Constants.NUMBER_OF_COMPANIES) {
                            if (stocksOwnedMap.containsKey(i)) {
                                stocksOwnedList.add(StockDetails(i, stocksOwnedMap.getValue(i)))
                            }
                        }

                        // Adding user's reserved assets details
                        val reservedStocksList = ArrayList<StockDetails>(30)
                        val reservedStocksMap = loginResponse.reservedStocksOwnedMap.orEmpty()

                        for (i in 1..Constants.NUMBER_OF_COMPANIES) {
                            if (reservedStocksMap.containsKey(i)) {
                                reservedStocksList.add(StockDetails(i, reservedStocksMap.getValue(i)))
                            }
                        }

                        // Adding global stock details
                        val globalStockList = ArrayList<GlobalStockDetails>()
                        val globalStockMap = loginResponse.stockListMap

                        for (q in 1..globalStockMap.size) {

                            val currentStockDetails = globalStockMap[q]

                            if (currentStockDetails != null) {
                                globalStockList.add(GlobalStockDetails(
                                        currentStockDetails.fullName,
                                        currentStockDetails.shortName,
                                        q,
                                        currentStockDetails.description,
                                        currentStockDetails.currentPrice,
                                        currentStockDetails.stocksInMarket,
                                        currentStockDetails.stocksInExchange,
                                        currentStockDetails.previousDayClose,
                                        if (currentStockDetails.upOrDown) 1 else 0,
                                        Constants.COMPANY_IMAGES_BASE_URL + currentStockDetails.shortName.toUpperCase() + ".png"))
                            }
                        }

                        lateinit var intent: Intent
                        if(loginResponse.user.isPhoneVerified) {
                            intent = Intent(this@LoginActivity, MainActivity::class.java)
                        }
                        else
                        {
                            intent = Intent(this@LoginActivity,VerifyPhoneActivity::class.java)
                        }
                            intent.putExtra(Constants.USERNAME_KEY, loginResponse.user.name)
                            intent.putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.user.cash)
                            intent.putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.user.total)
                            intent.putExtra(MainActivity.RESERVED_CASH_KEY, loginResponse.user.reservedCash)
                            intent.putExtra(Constants.MARKET_OPEN_KEY, loginResponse.isMarketOpen)

                            intent.putParcelableArrayListExtra(MainActivity.STOCKS_OWNED_KEY, stocksOwnedList)
                            intent.putParcelableArrayListExtra(MainActivity.GLOBAL_STOCKS_KEY, globalStockList)
                            intent.putParcelableArrayListExtra(MainActivity.RESERVED_STOCKS_KEY, reservedStocksList)

                            for ((key, value) in loginResponse.constantsMap) {
                                when (key) {
                                    "MORTGAGE_DEPOSIT_RATE" -> Constants.MORTGAGE_DEPOSIT_RATE = value.toDouble()
                                    "MORTGAGE_RETRIEVE_RATE" -> Constants.MORTGAGE_RETRIEVE_RATE = value.toDouble()
                                    "ORDER_FEE_PERCENT" -> Constants.ORDER_FEE_RATE = (value.toDouble() / 100)
                                    "ORDER_PRICE_WINDOW" -> Constants.ORDER_PRICE_WINDOW = value
                                }
                            }

                            preferences.edit()
                                    .putString(Constants.MARKET_OPEN_TEXT_KEY, loginResponse.marketIsOpenHackyNotif)
                                    .putString(Constants.MARKET_CLOSED_TEXT_KEY, loginResponse.marketIsClosedHackyNotif)
                                    .apply()
                            startActivity(intent)
                            finish()
                    } else {
                        toast(loginResponse.statusMessage)
                        passwordEditText.setText("")
                    }
                }
            } else {
                uiThread {
                    signingInAlertDialog?.dismiss()
                    contentView?.hideKeyboard()
                    showSnackBar("Server Unreachable")
                }
            }
        }
    }

    private fun sendForgotPasswordRequestAsynchronously(email: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@LoginActivity) }) {
            val forgotPasswordRequest = ForgotPasswordRequest.newBuilder().setEmail(email).build()
            val forgotPasswordResponse = withContext(Dispatchers.IO) { DalalActionServiceGrpc.newBlockingStub(channel).forgotPassword(forgotPasswordRequest) }

            toast(forgotPasswordResponse.statusMessage)

            if (forgotPasswordResponse.statusCode == ForgotPasswordResponse.StatusCode.InvalidCredentialsError) {
                onForgotPasswordClick()
            }

        } else {
            contentView?.hideKeyboard()
            toast("Server Unreachable")
        }
    }

    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { startLoginProcess(true) }

        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }
}
