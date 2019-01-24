package org.pragyan.dalal18.utils

import android.animation.TypeEvaluator

class LongEvaluator : TypeEvaluator<Long> {
    override fun evaluate(fraction: Float, startValue: Long, endValue: Long): Long {
        return ((endValue.toFloat() - startValue.toFloat()) * fraction).toLong() + startValue
    }
}