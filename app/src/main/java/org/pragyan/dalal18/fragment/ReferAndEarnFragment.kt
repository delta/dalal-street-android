package org.pragyan.dalal18.fragment

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetReferralCodeRequest
import dalalstreet.api.actions.GetReferralCodeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.databinding.FragmentReferAndEarnBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

class ReferAndEarnFragment : Fragment() {

    private var binding by viewLifecycle<FragmentReferAndEarnBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private var referralCodeDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReferAndEarnBinding.inflate(inflater, container, false)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogView = LayoutInflater.from(context!!).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.get_referral_code)
        referralCodeDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        binding.shareButton.setOnClickListener { shareReferralCode() }

        binding.referralCodeButton.setOnClickListener { copyReferralCodeToClipboard(binding.userReferralCodeEditText.hint.toString()) }

        getUserReferralCode()
    }

    private fun copyReferralCodeToClipboard(code: String) {
        if (code.isEmpty()) {
            Toast.makeText(context!!, "referesh your code", Toast.LENGTH_LONG).show()
            return
        }

        val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied referral code", code) as ClipData
        clipboardManager.setPrimaryClip(clip)

        Toast.makeText(context!!, "Referral code copied to clipboard", Toast.LENGTH_LONG).show()
    }

    private fun getUserReferralCode() = lifecycleScope.launch {
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context!!) }) {
            val email = preferences.getString(Constants.EMAIL_KEY, "").toString()

            if (email.isNotEmpty() && email.isNotBlank()) {
                referralCodeDialog?.show()
                getUserReferralCodeAsynchronously(email)
            }
        }
    }

    private fun getUserReferralCodeAsynchronously(userEmail: String) = lifecycleScope.launch {
        val referralCodeRequest = GetReferralCodeRequest.newBuilder()
                .setEmail(userEmail)
                .build()

        if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
            val referralCodeResponse = withContext(Dispatchers.IO) { actionServiceBlockingStub.getReferralCode(referralCodeRequest) }
            referralCodeDialog?.dismiss()

            if (referralCodeResponse.statusCode == GetReferralCodeResponse.StatusCode.OK) {
                binding.userReferralCodeEditText.hint = referralCodeResponse.referralCode.toString()

                preferences.edit().putString(Constants.USER_REFERRAL_CODE, referralCodeResponse.referralCode.toString()).apply()

                // copyReferralCodeToClipboard(referralCodeResponse.referralCode.toString())

            } else {
                Toast.makeText(context!!, referralCodeResponse.statusMessage.toString(), Toast.LENGTH_LONG).show()
            }

        } else {
            referralCodeDialog?.dismiss()
            showSnackBar("Server Unreachable", userEmail)
        }
    }

    private fun showSnackBar(message: String, userEmail: String) {
        val snackBar = Snackbar.make(activity!!.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { getUserReferralCode() }

        snackBar.setActionTextColor(ContextCompat.getColor(context!!, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }

    private fun shareReferralCode() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        val referralCode = preferences.getString(Constants.USER_REFERRAL_CODE, "")
        val shareMessage = "${Constants.DALAL_SHARE_MESSAGE}$referralCode and get instant cash reward!"

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Share referral code via..."))
    }
}
