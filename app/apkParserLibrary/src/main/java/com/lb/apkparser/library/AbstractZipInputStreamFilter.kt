package com.lb.apkparser.library

import java.io.Closeable
import java.util.*

abstract class AbstractZipInputStreamFilter : Closeable {
    abstract fun getNextEntryName(): String?
    abstract fun getBytesFromCurrentEntry(): ByteArray?

    /**Note: depending on the implementation, this might be usable only once, and before any other function (because once you call it, there is no turning back)
     * if there is an error of any kind, null might be returned*/
    open fun getByteArrayForEntries(mandatoryEntriesNames: Set<String>, extraEntriesNames: Set<String>? = null): HashMap<String, ByteArray>? {
        try {
            var remainingMandatoryNames = mandatoryEntriesNames.size
            val totalItemsCount = remainingMandatoryNames + (extraEntriesNames?.size ?: 0)
            val result = HashMap<String, ByteArray>(totalItemsCount)
            while (true) {
                val entryName = getNextEntryName()
                if (entryName == null) {
                    if (remainingMandatoryNames == 0)
                        return result
                    return null
                }
                val foundMandatoryEntry = mandatoryEntriesNames.contains(entryName)
                if (foundMandatoryEntry || extraEntriesNames?.contains(entryName) == true) {
                    val bytesFromCurrentEntry = getBytesFromCurrentEntry()
                    if (bytesFromCurrentEntry != null) {
                        if (foundMandatoryEntry)
                            --remainingMandatoryNames
                        result[entryName] = bytesFromCurrentEntry
                        if (result.size == totalItemsCount)
                            return result
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

}
