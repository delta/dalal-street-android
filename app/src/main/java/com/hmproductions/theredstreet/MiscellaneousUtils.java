package com.hmproductions.theredstreet;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class MiscellaneousUtils {

    public static float convertDpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
