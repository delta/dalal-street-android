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

    @Inject
    lateinit var preferences: SharedPreferences

    var email : String? = String()
    var password : String? = String()

    private var signingInAlertDialog: AlertDialog? = null

    lateinit var mobNumber: String
    lateinit var phoneNumberEditText: EditText
    lateinit var resendOtp: Button
    lateinit var verifyOtp: Button
    lateinit var otpEditText: OtpEditText

    companion object {
        fun newInstance(phNum: String, UID: String?, pass: String?) : OTPVerificationDialogFragment  {

            val f = OTPVerificationDialogFragment()
            f.mobNumber = phNum
            f.email = UID
            f.password = pass

            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        signingInAlertDialog = AlertDialog.Builder(context!!).setView(R.layout.progress_dialog).setCancelable(false).create()

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
                signingInAlertDialog?.show()



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

                        var intent = Intent(activity, MainActivity::class.java)
                        //  if(loginResponse.user.isPhoneVerified)

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
                        activity?.finish()

                    } else {
                        Toast.makeText(context,loginResponse.statusMessage,Toast.LENGTH_SHORT).show()
                        passwordEditText.setText("")
                    }
                }
            } else {
                uiThread {
                    signingInAlertDialog?.dismiss()
                    Toast.makeText(context,"Server Unreachable.",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}