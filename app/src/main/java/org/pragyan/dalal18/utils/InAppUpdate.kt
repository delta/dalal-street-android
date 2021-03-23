package org.pragyan.dalal18.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.pragyan.dalal18.R

/**
 * This class is responsible for updating the app from Google's playstore
 * without hindering the user experience. No need to goto playstore again
 * for updating your android app.
 *
 *
 * @param activity parent-activity from which instance of this class will be initialized.
 *
 *
 * Currently in testing phase, needs more extensive testing.
 *
 * Using FLEXIBLE policy for update, so that user experience will not be hindered.
 */
class InAppUpdate(activity: Activity) : InstallStateUpdatedListener {

    private var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE = 500
    private var parentActivity: Activity = activity

    private var currentType = AppUpdateType.FLEXIBLE

    init {

        appUpdateManager = AppUpdateManagerFactory.create(parentActivity)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

            // Check if update is available
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) { // UPDATE IS AVAILABLE

                // Priority: 5 (Immediate update flow)
                if (info.updatePriority() == 5) {
                    if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        Log.d("APP-UPDATE", "STARTING IN-APP UPDATE PROCESS")
                        startUpdate(info, AppUpdateType.IMMEDIATE)
                    }
                } else if (info.updatePriority() == 4) {

                    val clientVersionStalenessDays = info.clientVersionStalenessDays()

                    if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 5 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        // Trigger IMMEDIATE flow
                        startUpdate(info, AppUpdateType.IMMEDIATE)

                    } else if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 3 && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                        // Trigger FLEXIBLE flow
                        startUpdate(info, AppUpdateType.FLEXIBLE)

                    }
                } else if (info.updatePriority() == 3) {

                    val clientVersionStalenessDays = info.clientVersionStalenessDays()

                    if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 30 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        // Trigger IMMEDIATE flow
                        startUpdate(info, AppUpdateType.IMMEDIATE)

                    } else if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 15 && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                        // Trigger FLEXIBLE flow
                        startUpdate(info, AppUpdateType.FLEXIBLE)
                    }
                } else if (info.updatePriority() == 2) {

                    val clientVersionStalenessDays = info.clientVersionStalenessDays()

                    if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 90 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        // Trigger IMMEDIATE flow
                        startUpdate(info, AppUpdateType.IMMEDIATE)

                    } else if (clientVersionStalenessDays != null && clientVersionStalenessDays >= 30 && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                        // Trigger FLEXIBLE flow
                        startUpdate(info, AppUpdateType.FLEXIBLE)
                    }
                } else if (info.updatePriority() == 1) {

                    // Trigger FLEXIBLE flow
                    startUpdate(info, AppUpdateType.FLEXIBLE)
                } else {
                    // Do not show in-app update
                }
            } else {

                // App update is not available, no new version of the is not detected
                Log.d("APP-UPDATE", "APP UPDATE IS NOT AVAILABLE AT THIS MOMENT")
            }
        }
        appUpdateManager.registerListener(this)
    }


    private fun startUpdate(info: AppUpdateInfo, type: Int) {
        appUpdateManager.startUpdateFlowForResult(info, type, parentActivity, MY_REQUEST_CODE)
        currentType = type
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (currentType == AppUpdateType.FLEXIBLE) {

                // If the update is downloaded but not installed, notify the user to complete the update.
                if (info.installStatus() == InstallStatus.DOWNLOADED)
                    flexibleUpdateDownloadCompleted()
            } else if (currentType == AppUpdateType.IMMEDIATE) {

                // for AppUpdateType.IMMEDIATE only, already executing updater
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startUpdate(info, AppUpdateType.IMMEDIATE)
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {

                // If the update is cancelled or fails, you can request to start the update again.
                Log.e("ERROR", "Update flow failed! Result code: $resultCode")
            }
        }
    }

    private fun flexibleUpdateDownloadCompleted() {
        Snackbar.make(
                parentActivity.findViewById(android.R.id.content),
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(ContextCompat.getColor(parentActivity, R.color.neon_green))
            view.setBackgroundColor(Color.parseColor("#20202C"))
            show()
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(this)
    }

    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            flexibleUpdateDownloadCompleted()
        }
    }

}