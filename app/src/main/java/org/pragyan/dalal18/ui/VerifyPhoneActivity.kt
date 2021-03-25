package org.pragyan.dalal18.ui

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.LoginRequest
import dalalstreet.api.actions.LoginResponse
import dalalstreet.api.actions.LogoutRequest
import dalalstreet.api.actions.LogoutResponse
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.SmsVerificationPagerAdapter
import org.pragyan.dalal18.adapter.pagerAdapters.SmsVerificationPagerAdapter.Companion.ADD_PHONE
import org.pragyan.dalal18.adapter.pagerAdapters.SmsVerificationPagerAdapter.Companion.OTP_VERIFICATION
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.databinding.ActivityVerifyPhoneBinding
import org.pragyan.dalal18.fragment.smsVerification.AddPhoneFragment
import org.pragyan.dalal18.fragment.smsVerification.OTPVerificationFragment
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.Constants.SMS_KEY
import org.pragyan.dalal18.utils.MiscellaneousUtils
import org.pragyan.dalal18.utils.MiscellaneousUtils.convertDpToPixel
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

class VerifyPhoneActivity : AppCompatActivity(), ConnectionUtils.SmsVerificationHandler, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val binding by viewLifecycle(ActivityVerifyPhoneBinding::inflate)

    /* Not injecting stub directly into this context to prevent empty/null metadata attached to stub since user has not logged in. */
    @Inject
    lateinit var channel: ManagedChannel

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private var phoneNumber = ""
    private val LOG_TAG = VerifyPhoneActivity::class.java.simpleName

    private val newSMSReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.NEW_SMS_RECEIVED_ACTION) {
                val message = intent.getStringExtra(SMS_KEY) ?: ""

                Log.v(LOG_TAG, message)

                val otp = extractOtpFromMessage(message)
                val page = supportFragmentManager.findFragmentByTag(
                        "android:switcher:" + R.id.smsViewPager + ":" + binding.smsViewPager.currentItem) as OTPVerificationFragment

                Log.v(LOG_TAG, "Extracted OTP: $otp")
                page.checkIfOtpIsCorrect(otp, phoneNumber)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val toolbar = findViewById<Toolbar>(R.id.verifyOtpToolbar)
        setSupportActionBar(toolbar)
        title = getString(R.string.otp_verification)

        binding.apply {
            smsViewPager.adapter = SmsVerificationPagerAdapter(supportFragmentManager)
            smsTabLayout.setupWithViewPager(smsViewPager)

            smsTabLayout.setBackgroundColor(Color.parseColor("#20202C"))
        }

        for (i in 0 until binding.smsTabLayout.tabCount) {
            val tab: View = (binding.smsTabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val marginLayoutParams = tab.layoutParams as MarginLayoutParams

            val pixels = convertDpToPixel(8.0f, this)
            marginLayoutParams.setMargins(pixels, pixels, pixels, pixels)

            tab.requestLayout()
            tab.setOnTouchListener { _, _ -> true }
        }

        requestHint()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.verify_otp_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_otp_logout) logout()
        return super.onOptionsItemSelected(item)
    }

    private fun requestHint() {
        val mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build()

        val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
        val intent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest)
        startIntentSenderForResult(intent.intentSender, RESOLVE_HINT_RC, null, 0, 0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT_RC) {
            if (resultCode == Activity.RESULT_OK) {
                val phoneNumberWithExtension: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)

                if (phoneNumberWithExtension != null) {

                    startListeningToSMS()

                    val page = supportFragmentManager.findFragmentByTag(
                            "android:switcher:" + R.id.smsViewPager + ":" + binding.smsViewPager.currentItem) as AddPhoneFragment

                    phoneNumber = phoneNumberWithExtension.id
                    page.sendAddPhoneNumberAsynchronously(phoneNumberWithExtension.id)
                }
            }
        }
    }

    private fun startListeningToSMS() {

        val client = SmsRetriever.getClient(this)
        val task = client.startSmsRetriever()

        task.addOnSuccessListener { Log.v(LOG_TAG, "Expect a broadcast intent") }
        task.addOnFailureListener { Log.v(LOG_TAG, "Failed to start SMS retriever") }
    }

    fun logout() = lifecycleScope.launch {
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@VerifyPhoneActivity) }) {
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                val logoutResponse = withContext(Dispatchers.IO) {
                    actionServiceBlockingStub.logout(LogoutRequest.newBuilder().build())
                }

                if (logoutResponse.statusCode == LogoutResponse.StatusCode.OK) {
                    preferences.edit().putString(Constants.EMAIL_KEY, null).putString(Constants.PASSWORD_KEY, null).putString(Constants.SESSION_KEY, null).apply()
                }

                startActivity(Intent(this@VerifyPhoneActivity, LoginActivity::class.java))
                finish()
            } else {
                onNetworkDownError(resources.getString(R.string.error_server_down))
            }
        } else {
            onNetworkDownError(resources.getString(R.string.error_check_internet))
        }
    }

    private fun extractOtpFromMessage(message: String): String {
        return message.substringAfter("is ").substring(0, 4)
    }

    override fun navigateToOtpVerification(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        binding.smsViewPager.currentItem = OTP_VERIFICATION
    }

    override fun navigateToAddPhone() {
        this.phoneNumber = ""
        binding.smsViewPager.currentItem = ADD_PHONE
    }

    override fun getPhoneNumber(): String {
        return phoneNumber
    }

    override fun phoneVerificationSuccessful() {

        startLoginProcess(preferences.getString(SplashActivity.EMAIL_KEY, null), preferences.getString(SplashActivity.PASSWORD_KEY, null))
        //startActivity(intent)
        //finish()
    }

    private fun loginAsynchronously(email: String, password: String) {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@VerifyPhoneActivity)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val sessionId = preferences.getString(Constants.SESSION_KEY, null)

                    if (sessionId != null) {

                        val customStub = MetadataUtils.attachHeaders(DalalActionServiceGrpc.newBlockingStub(channel), getStubMetadata(sessionId))
                        val loginResponse = customStub.login(LoginRequest.newBuilder().setEmail(email).setPassword(password).build())

                        uiThread {

                            if (loginResponse.statusCode == LoginResponse.StatusCode.OK && !loginResponse.user.isBlocked) {

                                MiscellaneousUtils.sessionId = loginResponse.sessionId

                                val intent = Intent(this@VerifyPhoneActivity, MainActivity::class.java)
                                intent.putExtras(getIntent())
                                with(intent) {
                                    putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.user.cash)
                                    putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.user.total)
                                    putExtra(MainActivity.RESERVED_CASH_KEY, loginResponse.user.reservedCash)
                                }
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                         else {
                            uiThread { startLoginActivity() }
                        }
                    } else {
                        val snackBar = Snackbar.make(findViewById<View>(android.R.id.content), resources.getString(R.string.error_server_down), Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY") {
                                    startLoginProcess(email, password)
                                }

                        snackBar.setActionTextColor(ContextCompat.getColor(this@VerifyPhoneActivity, R.color.neon_green))
                        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
                        snackBar.show()

                    }

                } else /* No internet available */ {
                    uiThread {

                        val snackBar = Snackbar.make(findViewById<View>(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY") {
                                    startLoginProcess(email, password)

                                }

                        snackBar.setActionTextColor(ContextCompat.getColor(this@VerifyPhoneActivity, R.color.neon_green))
                        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
                        snackBar.show()

                    }
                }
            }
        }

    private fun startLoginProcess(email: String?, password: String?) {

        if (email != null && password != null && email != "") {
            loginAsynchronously(email, password)
        } else {
            startLoginActivity()
        }
    }

    private fun startLoginActivity() {
        preferences.edit().putString(SplashActivity.EMAIL_KEY, null).putString(SplashActivity.PASSWORD_KEY, null).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun getStubMetadata(sessionId: String): Metadata {
        val metadata = Metadata()
        val metadataKey = Metadata.Key.of("sessionid", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(metadataKey, sessionId)
        return metadata
    }


    override fun onNetworkDownError(message: String) {
        toast(message)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.NEW_SMS_RECEIVED_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(newSMSReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newSMSReceiver)
    }

    override fun onConnected(p0: Bundle?) {
        Log.d(LOG_TAG, "onConnected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(LOG_TAG, "onConnectionSuspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed")
    }

    companion object {
        const val RESOLVE_HINT_RC = 144
    }
}