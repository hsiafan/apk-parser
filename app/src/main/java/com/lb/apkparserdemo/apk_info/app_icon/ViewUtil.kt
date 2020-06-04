package com.lb.apkparserdemo.apk_info.app_icon

import android.content.Context
import android.util.TypedValue

object ViewUtil {
    fun convertDpToPixels(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}
