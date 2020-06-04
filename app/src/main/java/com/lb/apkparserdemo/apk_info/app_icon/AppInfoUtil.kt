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
        appIconSize = try {
            activityManager.launcherLargeIconSize
        } catch (e: Exception) {
            ViewUtil.convertDpToPixels(context, 48f).toInt()
        }
        return appIconSize
    }
}
