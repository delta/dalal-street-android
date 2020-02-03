package org.pragyan.dalal18.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.LogoutRequest
import dalalstreet.api.actions.LogoutResponse
import kotlinx.android.synthetic.main.activity_verify_phone.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.SmsVerificationPagerAdapter
import org.pragyan.dalal18.adapter.SmsVerificationPagerAdapter.Companion.ADD_PHONE
import org.pragyan.dalal18.adapter.SmsVerificationPagerAdapter.Companion.OTP_VERIFICATION
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils.convertDpToPixel
import javax.inject.Inject


class VerifyPhoneActivity : AppCompatActivity(), ConnectionUtils.SmsVerificationHandler {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private var phoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        val toolbar = findViewById<Toolbar>(R.id.verifyOtpToolbar)
        setSupportActionBar(toolbar)
        title = getString(R.string.otp_verification)

        smsViewPager.adapter = SmsVerificationPagerAdapter(supportFragmentManager)
        smsTabLayout.setupWithViewPager(smsViewPager)

        smsTabLayout.setBackgroundColor(Color.parseColor("#20202C"))

        for (i in 0 until smsTabLayout.tabCount) {
            val tab: View = (smsTabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val marginLayoutParams = tab.layoutParams as MarginLayoutParams

            val pixels = convertDpToPixel(8.0f, this)
            marginLayoutParams.setMargins(pixels, pixels, pixels, pixels)

            tab.requestLayout()
            tab.setOnTouchListener { _, _ -> true }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.verify_otp_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_otp_logout) logout()
        return super.onOptionsItemSelected(item)
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

    override fun navigateToOtpVerification(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        smsViewPager.currentItem = OTP_VERIFICATION
    }

    override fun navigateToAddPhone() {
        this.phoneNumber = ""
        smsViewPager.currentItem = ADD_PHONE
    }

    override fun getPhoneNumber(): String {
        return phoneNumber
    }

    override fun onNetworkDownError(message: String) {
        // TODO: Internet unavailable
    }
}