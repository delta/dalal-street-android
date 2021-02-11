package org.pragyan.dalal18.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewUtils {

    fun requestInAppReviewFromUser(context: Context) {
        val reviewManager = ReviewManagerFactory.create(context)

        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                Log.d("USER REVIEW", reviewInfo.toString())

                val reviewFlow = reviewManager.launchReviewFlow(context as Activity, reviewInfo)
                reviewFlow.addOnCompleteListener { _ ->
                    Log.d("REVIEW FLOW COMPLETED", "DONE YEAH !")
                }
            } else {
                // Toast.makeText(context, request.exception.toString(), Toast.LENGTH_LONG).show()
                Log.d("ERROR:", request.exception.toString())
            }
        }

    }
}