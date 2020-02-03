package org.pragyan.dalal18.adapter.pagerAdapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.pragyan.dalal18.fragment.smsVerification.AddPhoneFragment
import org.pragyan.dalal18.fragment.smsVerification.OTPVerificationFragment

class SmsVerificationPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AddPhoneFragment()
            else -> OTPVerificationFragment()
        }
    }

    override fun getCount() = NUMBER_OF_FRAGMENTS

    override fun getPageTitle(position: Int) = (position + 1).toString()

    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
        const val ADD_PHONE = 0
        const val OTP_VERIFICATION = 1
    }
}