package com.lb.apkparser

abstract class AbstractZipInputStreamFilter {
    abstract fun getNextEntryName(): String?
    abstract fun getBytesFromCurrentEntry(): ByteArray?
}
