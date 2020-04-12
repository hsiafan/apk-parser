package com.lb.apkparser.library

import java.io.Closeable
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipFileFilter(private val zipFile: ZipFile) : AbstractZipInputStreamFilter(), Closeable {
    private var entries: Enumeration<out ZipEntry>? = null
    var currentEntry: ZipEntry? = null

    init {
        try {
            this.entries = zipFile.entries()
        } catch (e: Exception) {
            close()
        }
    }

    override fun getByteArrayForEntries(mandatoryEntriesNames: Set<String>, extraEntriesNames: Set<String>?): HashMap<String, ByteArray>? {
        try {
            val totalItemsCount = mandatoryEntriesNames.size + (extraEntriesNames?.size ?: 0)
            val result = HashMap<String, ByteArray>(totalItemsCount)
            for (name in mandatoryEntriesNames) {
                val entry: ZipEntry? = zipFile.getEntry(name) ?: return null
                result[name] = zipFile.getInputStream(entry).readBytes()
            }
            if (extraEntriesNames != null)
                for (name in extraEntriesNames) {
                    val entry: ZipEntry? = zipFile.getEntry(name) ?: continue
                    result[name] = zipFile.getInputStream(entry).readBytes()
                }
            return result
        } catch (e: Exception) {
            return null
        }
    }


    override fun getNextEntryName(): String? {
        entries.let {
            if (it == null)
                return null
            try {
                it.nextElement().let { zipEntry: ZipEntry? ->
                    if (zipEntry == null) {
//                        close()
                        currentEntry = null
                        entries = null
                        return null
                    }
                    currentEntry = zipEntry
                    return zipEntry.name
                }
            } catch (e: Exception) {
                currentEntry = null
                entries = null
//                close()
                return null
            }
        }
    }

    override fun getBytesFromCurrentEntry(): ByteArray? {
        currentEntry.let { zipEntry: ZipEntry? ->
            if (zipEntry == null)
                return null
            return try {
                zipFile.getInputStream(zipEntry).readBytes()
            } catch (e: Exception) {
                close()
                null
            }
        }
    }

    override fun close() {
        entries = null
        currentEntry = null
        try {
            zipFile.close()
        } catch (e: Exception) {
        }
    }

}
