package com.lb.apkparser.library

import java.io.Closeable
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipInputStreamFilter(private val zipInputStream: ZipInputStream) : AbstractZipInputStreamFilter(), Closeable {
    private var currentEntry: ZipEntry? = null
    private var currentEntryByteArray: ByteArray? = null

    override fun getNextEntryName(): String? {
        try {
            zipInputStream.nextEntry.let { zipEntry: ZipEntry? ->
                if (zipEntry == null) {
                    close()
                    return null
                }
                currentEntry = zipEntry
                currentEntryByteArray = null
                return zipEntry.name
            }
        } catch (e: Exception) {
            close()
            return null
        }
    }

    override fun getBytesFromCurrentEntry(): ByteArray? {
        currentEntryByteArray?.let { return it }
        try {
            currentEntry.let { zipEntry: ZipEntry? ->
                if (zipEntry == null) {
                    close()
                    return null
                }
                zipInputStream.readBytes().let {
                    currentEntryByteArray = it
                    return it
                }
            }
        } catch (e: Exception) {
            close()
            return null
        }
    }

    override fun close() {
        currentEntry = null
        currentEntryByteArray = null
        try {
            zipInputStream.close()
        } catch (e: Exception) {
        }
    }
}
