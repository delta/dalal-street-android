package org.pragyan.dalal18.utils

import android.animation.TypeEvaluator
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import java.lang.StringBuilder
import java.util.*

class LongEvaluator : TypeEvaluator<Long> {
    override fun evaluate(fraction: Float, startValue: Long, endValue: Long): Long {
        return ((endValue.toFloat() - startValue.toFloat()) * fraction).toLong() + startValue
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun ImageView.setStatusIndicator(context: Context?, viewVisibility: Int, toastMessage: String, resId: Int) {
    visibility = viewVisibility
    setImageResource(resId)
    if (toastMessage.isNotBlank()) setOnClickListener { context?.toast(toastMessage) }
}

fun formatTransactionType(type: String): String {
    val formatted = StringBuilder(type)
    for(i in formatted.indices) {
        formatted[i] = formatted[i].toLowerCase()
        if(formatted[i] == '_') formatted[i] = ' '
        if(formatted[i].isLowerCase() && (i==0 || formatted[i-1] == ' ')) formatted[i] = formatted[i].toUpperCase()
    }
    return formatted.toString()
}