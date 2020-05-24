package com.lb.apkparserdemo.apk_info

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.Closeable

/**Seems to be the faster choice compared to the built in ZipInputStream*/
class ApacheZipArchiveInputStreamFilter(private val zipArchiveInputStream: ZipArchiveInputStream) : AbstractZipFilter(), Closeable {
    private var currentEntry: ArchiveEntry? = null
    private var currentEntryByteArray: ByteArray? = null

    override fun getNextEntryName(): String? {
        try {
            zipArchiveInputStream.nextEntry.let { zipEntry: ArchiveEntry? ->
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
            currentEntry.let { zipEntry: ArchiveEntry? ->
                if (zipEntry == null) {
                    close()
                    return null
                }
                zipArchiveInputStream.readBytes().let {
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
            zipArchiveInputStream.close()
        } catch (e: Exception) {
        }
    }
}
