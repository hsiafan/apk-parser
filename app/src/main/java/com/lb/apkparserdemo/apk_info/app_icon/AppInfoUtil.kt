package com.lb.apkparserdemo.apk_info.app_icon

import android.app.ActivityManager
import android.content.Context
import com.lb.apkparserdemo.getSystemService

object AppInfoUtil {
    private var appIconSize = 0

    fun getAppIconSize(context: Context): Int {
        if (appIconSize > 0)
            return appIconSize
        val activityManager = context.getSystemService<ActivityManager>()
        //https://console.firebase.google.com/u/0/project/app-manager-cdf2c/crashlytics/app/android:com.lb.app_manager/issues/d5c98cf94a3d87148b915413f2583657?time=last-seven-days&sessionId=5D4B95C60017000214A7E2D1546BCD11_DNE_0_v2
        //TODO maybe after API 6 it's not needed to use try-catch
        appIconSize = try {
            activityManager.launcherLargeIconSize
        } catch (e: Exception) {
            ViewUtil.convertDpToPixels(context, 48f).toInt()
        }
        return appIconSize
    }
}
