package com.lb.apkparser

import net.dongliu.apk.parser.parser.*
import net.dongliu.apk.parser.struct.AndroidConstants
import net.dongliu.apk.parser.struct.resource.ResourceTable
import java.nio.ByteBuffer
import java.util.*

class ApkInfo(val xmlTranslator: XmlTranslator, val apkMetaTranslator: ApkMetaTranslator, val apkType: ApkType?) {
    enum class ApkType {
        STANDALONE, BASE_OF_SPLIT, SPLIT, BASE_OF_SPLIT_OR_STANDALONE
    }

    companion object {
        @Suppress("SameParameterValue")
        fun getApkInfo(locale: Locale, zipInputStreamFilter: AbstractZipInputStreamFilter, requestParseManifestXmlTagForApkType: Boolean = false, requestParseResources: Boolean = false): ApkInfo? {
            var manifestBytes: ByteArray? = null
            var resourcesBytes: ByteArray? = null
            while (true) {
                val zipEntryName = zipInputStreamFilter.getNextEntryName() ?: break
                when (zipEntryName) {
                    AndroidConstants.MANIFEST_FILE -> {
                        manifestBytes = zipInputStreamFilter.getBytesFromCurrentEntry()
                        if (!requestParseResources || resourcesBytes != null)
                            break
                    }
                    AndroidConstants.RESOURCE_FILE -> {
                        if (!requestParseResources)
                            continue
                        resourcesBytes = zipInputStreamFilter.getBytesFromCurrentEntry()
                        if (manifestBytes != null)
                            break
                    }
                }
            }
            if (manifestBytes == null) {
//            Log.e("AppLog", "could not find manifest file for $apkFilePath")
                return null
            }
            val xmlTranslator = XmlTranslator()
            val resourceTable: ResourceTable =
                    if (resourcesBytes == null)
                        ResourceTable()
                    else {
                        val resourceTableParser = ResourceTableParser(ByteBuffer.wrap(resourcesBytes))
                        resourceTableParser.parse()
                        resourceTableParser.resourceTable
                        //                this.locales = resourceTableParser.locales
                    }
            val apkMetaTranslator = ApkMetaTranslator(resourceTable, locale)
            val binaryXmlParser = BinaryXmlParser(ByteBuffer.wrap(manifestBytes), resourceTable)
            binaryXmlParser.locale = locale
            binaryXmlParser.xmlStreamer = CompositeXmlStreamer(xmlTranslator, apkMetaTranslator)
            binaryXmlParser.parse()
            var apkType: ApkInfo.ApkType? = null
            if (requestParseManifestXmlTagForApkType) {
                val apkMeta = apkMetaTranslator.apkMeta
                val isSplitApk = !apkMeta.split.isNullOrEmpty()
                if (isSplitApk)
                    apkType = ApkInfo.ApkType.SPLIT
                else {
                    //standalone or base of split apks
                    val isDefinitelyBaseApkOfSplit = apkMeta.isSplitRequired
                    if (isDefinitelyBaseApkOfSplit)
                        apkType = ApkInfo.ApkType.BASE_OF_SPLIT
                    else {
                        val manifestXml = xmlTranslator.xml
                        apkType = ApkInfo.ApkType.STANDALONE
                        try {
                            XmlTag.getXmlFromString(manifestXml)?.innerTagsAndContent?.forEach { manifestXmlItem: Any ->
                                if (manifestXmlItem is XmlTag && manifestXmlItem.tagName == "application") {
                                    val innerTagsAndContent = manifestXmlItem.innerTagsAndContent
                                            ?: return@forEach
                                    for (applicationXmlItem: Any in innerTagsAndContent) {
                                        if (applicationXmlItem is XmlTag && applicationXmlItem.tagName == "meta-data"
                                                && applicationXmlItem.tagAttributes?.get("name") == "com.android.vending.splits") {
                                            apkType = ApkInfo.ApkType.BASE_OF_SPLIT_OR_STANDALONE
                                        }
                                        if (applicationXmlItem is XmlTag && applicationXmlItem.tagName == "meta-data"
                                                && applicationXmlItem.tagAttributes?.get("name") == "instantapps.clients.allowed" &&
                                                applicationXmlItem.tagAttributes!!["value"] != "false") {
                                            apkType = ApkInfo.ApkType.BASE_OF_SPLIT_OR_STANDALONE
                                        }
                                        if (applicationXmlItem is XmlTag && applicationXmlItem.tagName == "meta-data"
                                                && applicationXmlItem.tagAttributes?.get("name") == "com.android.vending.splits.required") {
                                            val isSplitRequired = applicationXmlItem.tagAttributes!!["value"] != "false"
//                                        if (!isSplitRequired)
//                                            Log.e("AppLog", "!isSplitRequired")
                                            apkType = if (isSplitRequired) ApkInfo.ApkType.BASE_OF_SPLIT else ApkInfo.ApkType.BASE_OF_SPLIT_OR_STANDALONE
//                                            apkType = ApkInfo.ApkType.BASE_OF_SPLIT
                                            break
                                        }
                                    }
                                }
                                return@forEach
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
//                            Log.e("AppLog", "failed to get apk type: $e")
                        }
                    }
                }
            }
            return ApkInfo(xmlTranslator, apkMetaTranslator, apkType)
        }
    }
}
