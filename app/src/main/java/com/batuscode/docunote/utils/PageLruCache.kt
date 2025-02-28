package com.batuscode.docunote.utils

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.LruCache

class PageLruCache(maxSize: Int) : LruCache<String, Bitmap>(maxSize) {

    override fun sizeOf(key: String?, value: Bitmap?): Int {
        return super.sizeOf(key, value)
    }

    override fun entryRemoved(
        evicted: Boolean,
        key: String?,
        oldValue: Bitmap?,
        newValue: Bitmap?
    ) {
        super.entryRemoved(evicted, key, oldValue, newValue)
        oldValue?.recycle()
    }

    fun getCacheSize(): Int {
        val maxMemory = Runtime.getRuntime().maxMemory() // En fazla kullanılabilir bellek (bytes)
        val cacheSize = maxMemory / 8 // 1/8'ini cache için ayırıyoruz.
        return cacheSize.toInt()
    }


}