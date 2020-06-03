package com.lb.apkparserdemo.apk_info.app_icon

import android.graphics.BitmapFactory
import kotlin.math.roundToInt

object BitmapHelper {
    fun prepareBitmapOptionsForSampling(bitmapOptions: BitmapFactory.Options, reqWidth: Int, reqHeight: Int) {
        bitmapOptions.inTargetDensity = 1
        bitmapOptions.inJustDecodeBounds = false
        if (reqHeight <= 0 && reqWidth <= 0)
            return
        bitmapOptions.inDensity = 1
        var sampleSize = 1
        bitmapOptions.inSampleSize = 1
        val height = bitmapOptions.outHeight
        val width = bitmapOptions.outWidth
        var preferHeight = false
        bitmapOptions.inDensity = 1
        bitmapOptions.inTargetDensity = 1
        if (height <= reqHeight && width <= reqWidth)
            return
        if (height > reqHeight || width > reqWidth)
            if (width > height && reqHeight >= 1) {
                preferHeight = true
                sampleSize = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            } else if (reqWidth >= 1) {
                sampleSize = (width.toFloat() / reqWidth.toFloat()).roundToInt()
                preferHeight = false
            }
        // as much as possible, use google's way to downsample:
        while (bitmapOptions.inSampleSize * 2 <= sampleSize)
            bitmapOptions.inSampleSize *= 2
        // if google's way to downsample isn't enough, do some more :
        if (bitmapOptions.inSampleSize != sampleSize) {
            // downsample by bitmapOptions.inSampleSize/originalSampleSize .
            bitmapOptions.inTargetDensity = bitmapOptions.inSampleSize
            bitmapOptions.inDensity = sampleSize
        } else if (sampleSize == 1) {
            bitmapOptions.inTargetDensity = if (preferHeight) reqHeight else reqWidth
            bitmapOptions.inDensity = if (preferHeight) height else width
        }
    }
}
