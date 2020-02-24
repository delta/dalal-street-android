package org.pragyan.dalal18.utils

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import org.pragyan.dalal18.R

object DalalTourUtils {

    fun toolbarTour(mainToolbar: androidx.appcompat.widget.Toolbar, context: AppCompatActivity, radius: Int, string: String) {
        TapTargetView.showFor(context,
                TapTarget.forToolbarMenuItem(mainToolbar, R.id.action_notifications, string)
                        .cancelable(true)
                        .tintTarget(true)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.neutral_font_color)
                        .targetCircleColor(R.color.neutral_font_color)
                        .textColor(R.color.neon_green)
                        .textTypeface(Typeface.MONOSPACE)
                        .drawShadow(true)
                        .transparentTarget(true)
                        .targetRadius(radius),
                object : TapTargetView.Listener() {

                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view)
                        view?.dismiss(true)
                    }
                })
    }

    // general function for tour , can be call with any view
    fun genericViewTour(context: AppCompatActivity, view: View, radiusInDp: Int, string: String) {
        TapTargetView.showFor(context, TapTarget.forView(view, string)
                .cancelable(true)
                .tintTarget(true)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(R.color.neutral_font_color)
                .targetCircleColor(R.color.neutral_font_color)
                .textColor(R.color.neon_green)
                .textTypeface(Typeface.MONOSPACE)
                .drawShadow(true)
                .transparentTarget(true)
                .targetRadius(radiusInDp),
                object : TapTargetView.Listener() {

                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view)
                        view?.dismiss(true)
                    }
                }
        )
    }

    fun depthFragmentTour(context: AppCompatActivity, x: LinearLayout, y: LinearLayout, stringX: String, stringY: String) {
        TapTargetSequence(context)
                .targets(
                        TapTarget.forView(x, stringX)
                                .cancelable(false).transparentTarget(true).targetRadius(70).textColor(R.color.neon_green)
                                .textTypeface(Typeface.MONOSPACE),
                        TapTarget.forView(y, stringY).cancelable(false).transparentTarget(true).targetRadius(70).cancelable(false).textColor(R.color.neon_green)
                                .textTypeface(Typeface.MONOSPACE)
                                .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    }

                    override fun onSequenceFinish() {

                    }

                    override fun onSequenceCanceled(lastTarget: TapTarget) {
                    }
                }).start()
    }
}

interface EndTutorialInterface {
    fun removeTutorial()
}