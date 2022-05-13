package com.lb.apkparserdemo.apk_info.app_icon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.lb.apkparserdemo.apk_info.AbstractZipFilter
import com.lb.apkparserdemo.apk_info.ApkInfo
import com.lb.apkparserdemo.apk_info.XmlDrawableParser
import net.dongliu.apk.parser.bean.IconPath
import net.dongliu.apk.parser.parser.AdaptiveIconParser
import net.dongliu.apk.parser.parser.BinaryXmlParser
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs

object ApkIconFetcher {
    interface ZipFilterCreator {
        fun generateZipFilter(): AbstractZipFilter
    }

    fun getApkIcon(
        context: Context,
        locale: Locale,
        filterGenerator: ZipFilterCreator,
        apkInfo: ApkInfo,
        requestedAppIconSize: Int = 0
    ): Bitmap? {
        val iconPaths = apkInfo.apkMetaTranslator.iconPaths
        if (iconPaths.isNullOrEmpty())
            return null
        val resources = context.resources
        val densityDpi = resources.displayMetrics.densityDpi
        var bestDividedDensityIconImage: IconPath? = null
        var bestDivider = 0
        var closestDensityMatchIconImage: IconPath? = null
        var bestDensityDiff = -1
        val xmlIconsPaths = HashSet<String>()
        for (iconPath in iconPaths) {
            if (iconPath.path.endsWith(".xml", true)) {
                xmlIconsPaths.add(iconPath.path)
                continue
            }
            if (iconPath.density % densityDpi == 0) {
                //divided nicely
                val divider = iconPath.density / densityDpi
                if (divider < bestDivider || bestDivider < 0) {
                    bestDivider = divider
                    bestDividedDensityIconImage = iconPath
                }
                if (bestDivider == 1)
                    break
            }
            val densityDiff = abs(iconPath.density - densityDpi)
            if (bestDensityDiff < 0 || densityDiff < bestDensityDiff) {
                bestDensityDiff = densityDiff
                closestDensityMatchIconImage = iconPath
            }
        }
        val iconsToFetch = HashSet<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            iconsToFetch.addAll(xmlIconsPaths)
        val imageIconPath = bestDividedDensityIconImage?.path
            ?: closestDensityMatchIconImage?.path
        imageIconPath?.let { iconsToFetch.add(it) }
        if (iconsToFetch.isEmpty())
            return null
        filterGenerator.generateZipFilter().use { filter: AbstractZipFilter ->
            val byteArrayForEntries = filter.getByteArrayForEntries(iconsToFetch) ?: return null
//            val requestedAppIconSize = getAppIconSize(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                for (xmlIconsPath in xmlIconsPaths) {
                    //prefer to try to parse XML first
                    val bytes = byteArrayForEntries[xmlIconsPath] ?: continue
//apkInfo.resourceTable
                    try {
                        val adaptiveIconParser = AdaptiveIconParser()
                        val buffer = ByteBuffer.wrap(bytes)
                        val binaryXmlParser = BinaryXmlParser(buffer, apkInfo.resourceTable)
                        binaryXmlParser.locale = locale
                        binaryXmlParser.xmlStreamer = adaptiveIconParser
                        binaryXmlParser.parse()
                        val backgroundPath: String? = adaptiveIconParser.background
                        val foregroundPath: String? = adaptiveIconParser.foreground
                        if (!backgroundPath.isNullOrBlank() && !foregroundPath.isNullOrBlank()) {
                            filterGenerator.generateZipFilter()
                                .use { adaptiveIconZipFilter: AbstractZipFilter ->
                                    adaptiveIconZipFilter.getByteArrayForEntries(
                                        hashSetOf(
                                            backgroundPath,
                                            foregroundPath
                                        )
                                    )?.let { adaptiveIconByteArrayForEntries ->
                                        val backgroundIconBytes =
                                            adaptiveIconByteArrayForEntries[backgroundPath]
                                        val foregroundIconBytes =
                                            adaptiveIconByteArrayForEntries[foregroundPath]
                                        if (backgroundIconBytes != null && foregroundIconBytes != null) {
                                            val backgroundDrawable =
                                                if (backgroundPath.endsWith(".xml", true))
                                                    XmlDrawableParser.tryParseDrawable(
                                                        context,
                                                        backgroundIconBytes
                                                    )
                                                else getAppIconFromByteArray(
                                                    backgroundIconBytes,
                                                    requestedAppIconSize
                                                )?.let { BitmapDrawable(resources, it) }
                                            val foregroundDrawable =
                                                if (foregroundPath.endsWith(".xml", true))
                                                    XmlDrawableParser.tryParseDrawable(
                                                        context,
                                                        foregroundIconBytes
                                                    )
                                                else getAppIconFromByteArray(
                                                    foregroundIconBytes,
                                                    requestedAppIconSize
                                                )?.let { BitmapDrawable(resources, it) }
                                            if (backgroundDrawable != null && foregroundDrawable != null) {
                                                val adaptiveIconDrawable = AdaptiveIconDrawable(
                                                    backgroundDrawable,
                                                    foregroundDrawable
                                                )
                                                return adaptiveIconDrawable.toBitmap(
                                                    requestedAppIconSize,
                                                    requestedAppIconSize
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            if (imageIconPath == null)
                return null
            val bytes = byteArrayForEntries[imageIconPath] ?: return null
            return getAppIconFromByteArray(bytes, requestedAppIconSize)
        }
    }

    private fun getAppIconFromByteArray(bytes: ByteArray, requestedAppIconSize: Int): Bitmap? {
        if (requestedAppIconSize > 0) {
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bitmapOptions)
            BitmapHelper.prepareBitmapOptionsForSampling(
                bitmapOptions,
                requestedAppIconSize,
                requestedAppIconSize
            )
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bitmapOptions)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
