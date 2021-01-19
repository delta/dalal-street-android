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
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
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

    private fun onLoginButtonClick() = lifecycleScope.launch {
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@LoginActivity) }) {
            if (validateEmail(emailEditText) && validatePassword()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                signingInAlertDialog?.show()
                loginAsynchronously(email, password)
            }
        } else {
            startLoginProcess(false)
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

    private fun loginAsynchronously(email: String, password: String) = lifecycleScope.launch {

        val loginRequest = LoginRequest
                .newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build()

        val stub = DalalActionServiceGrpc.newBlockingStub(channel)

        if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {

            val loginResponse = withContext(Dispatchers.IO) { stub.login(loginRequest) }

            signingInAlertDialog?.dismiss()

            if (loginResponse.statusCode == LoginResponse.StatusCode.OK) {

                if (loginResponse.user.isBlocked) {
                    showBlockedDialog()
                    return@launch
                }

                MiscellaneousUtils.sessionId = loginResponse.sessionId

                if (passwordEditText.text.toString() != "" || passwordEditText.text.toString().isNotEmpty())
                    preferences.edit()
                            .putString(Constants.EMAIL_KEY, loginResponse.user.email)
                            .putString(Constants.PASSWORD_KEY, passwordEditText.text.toString())
                            .putString(Constants.SESSION_KEY, loginResponse.sessionId)
                            .apply()

                // Adding user's stock details
                val ownedStockDetailsMap = hashMapOf<Int, Long>()
                for ((stockId, quantity) in loginResponse.stocksOwnedMap) {
                    ownedStockDetailsMap[stockId] = quantity
                }

                // Adding user's reserved assets details
                val reservedStockDetailsMap = hashMapOf<Int, Long>()
                for ((stockId, quantity) in loginResponse.reservedStocksOwnedMap) {
                    reservedStockDetailsMap[stockId] = quantity
                }

                // Adding global stock details
                val globalStockDetailsMap = hashMapOf<Int, GlobalStockDetails>()

                for ((stockId, currentStock) in loginResponse.stockListMap) {
                    globalStockDetailsMap[stockId] = GlobalStockDetails(
                            currentStock.fullName,
                            currentStock.shortName,
                            stockId,
                            currentStock.description,
                            currentStock.currentPrice,
                            currentStock.stocksInMarket,
                            currentStock.stocksInExchange,
                            currentStock.previousDayClose,
                            if (currentStock.upOrDown) 1 else 0,
                            currentStock.isBankrupt,
                            currentStock.givesDividends,
                            Constants.COMPANY_IMAGES_BASE_URL + currentStock.shortName.toUpperCase(Locale.ROOT) + ".png")
                }

                val intent: Intent = if (loginResponse.user.isPhoneVerified) {
                    Intent(this@LoginActivity, MainActivity::class.java)
                } else {
                    Intent(this@LoginActivity, VerifyPhoneActivity::class.java)
                }

                intent.putExtra(Constants.USERNAME_KEY, loginResponse.user.name)
                intent.putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.user.cash)
                intent.putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.user.total)
                intent.putExtra(MainActivity.RESERVED_CASH_KEY, loginResponse.user.reservedCash)
                intent.putExtra(Constants.MARKET_OPEN_KEY, loginResponse.isMarketOpen)

                intent.putExtra(MainActivity.STOCKS_OWNED_KEY, ownedStockDetailsMap)
                intent.putExtra(MainActivity.GLOBAL_STOCKS_KEY, globalStockDetailsMap)
                intent.putExtra(MainActivity.RESERVED_STOCKS_KEY, reservedStockDetailsMap)

                for ((key, value) in loginResponse.constantsMap) {
                    when (key) {
                        "MORTGAGE_DEPOSIT_RATE" -> Constants.MORTGAGE_DEPOSIT_RATE = value.toDouble()
                        "MORTGAGE_RETRIEVE_RATE" -> Constants.MORTGAGE_RETRIEVE_RATE = value.toDouble()
                        "ORDER_FEE_PERCENT" -> Constants.ORDER_FEE_RATE = (value.toDouble() / 100)
                        "ORDER_PRICE_WINDOW" -> Constants.ORDER_PRICE_WINDOW = value
                        "ASK_LIMIT" -> Constants.ASK_LIMIT = value
                        "BID_LIMIT" -> Constants.BID_LIMIT = value
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
        } else {
            signingInAlertDialog?.dismiss()
            contentView?.hideKeyboard()
            showSnackBar("Server Unreachable")
        }
    }

    private fun sendForgotPasswordRequestAsynchronously(email: String) = lifecycleScope.launch {

        signingInAlertDialog?.show()
        contentView?.hideKeyboard()

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@LoginActivity) }) {
            val forgotPasswordRequest = ForgotPasswordRequest.newBuilder().setEmail(email).build()
            val forgotPasswordResponse = withContext(Dispatchers.IO) { DalalActionServiceGrpc.newBlockingStub(channel).forgotPassword(forgotPasswordRequest) }

            signingInAlertDialog?.dismiss()
            AlertDialog.Builder(this@LoginActivity, R.style.AlertDialogTheme)
                    .setTitle("Forgot Password")
                    .setMessage(forgotPasswordResponse.statusMessage)
                    .setPositiveButton("OKAY") { dI, _ -> dI.dismiss() }
                    .setCancelable(true)
                    .show()

        } else {
            signingInAlertDialog?.dismiss()
            toast("Server Unreachable")
        }
    }

    private fun showBlockedDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Account Blocked")
                .setMessage("Your account has been blocked for violation of our terms. Contact admin for more information.")
                .setPositiveButton("OKAY") { dI, _ -> dI.dismiss() }
                .setCancelable(false)
                .show()
    }

    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { startLoginProcess(true) }

        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }
}
