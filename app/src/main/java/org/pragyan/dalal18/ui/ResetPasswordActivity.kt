package org.pragyan.dalal18.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.ChangePasswordRequest
import dalalstreet.api.actions.ChangePasswordResponse
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.databinding.ActivityResetPasswordBinding
import org.pragyan.dalal18.utils.*
import javax.inject.Inject

class ResetPasswordActivity : AppCompatActivity() {

    private val binding by viewLifecycle(ActivityResetPasswordBinding::inflate)

    @Inject
    lateinit var channel: ManagedChannel

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val toolbar = findViewById<Toolbar>(R.id.resetPasswordToolBar)
        setSupportActionBar(toolbar)
        title = getString(R.string.app_name)

        binding.resetPasswordButton.setOnClickListener { onResetPasswordButtonClick() }

        if (preferences.getString(Constants.EMAIL_KEY, null) != null) {
            longToast("User already logged in")
            finish()
        }
    }

    private fun onResetPasswordButtonClick() {
        binding.apply {
            when {
                temporaryPasswordEditText.text.toString().isBlank() || temporaryPasswordEditText.text.toString().isEmpty() -> toast("Enter temporary password from email")
                newPasswordEditText.text.toString().isBlank() || newPasswordEditText.text.toString().isEmpty() -> toast("Enter new password")
                confirmPasswordEditText.text.toString().isBlank() || confirmPasswordEditText.text.toString().isEmpty() ||
                        confirmPasswordEditText.text.toString() != newPasswordEditText.text.toString() -> toast("Confirm password failed")
                else -> sendChangePasswordRequestAsynchronously(
                        temporaryPasswordEditText.text.toString(),
                        newPasswordEditText.text.toString(),
                        confirmPasswordEditText.text.toString())
            }
        }
    }

    private fun sendChangePasswordRequestAsynchronously(tempPassword: String, newPassword: String, confirmPassword: String) = lifecycleScope.launch {

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@ResetPasswordActivity) }) {
            val changePasswordRequest = ChangePasswordRequest.newBuilder().setTempPassword(tempPassword).setNewPassword(newPassword).setConfirmPassword(confirmPassword).build()
            val changePasswordResponse = withContext(Dispatchers.IO) { DalalActionServiceGrpc.newBlockingStub(channel).changePassword(changePasswordRequest) }

            toast(changePasswordResponse.statusMessage)

            if (changePasswordResponse.statusCode == ChangePasswordResponse.StatusCode.OK) {
                startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                finish()
            }

        } else {
            binding.root.hideKeyboard()
            toast("Server Unreachable")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
        finish()
    }
}
