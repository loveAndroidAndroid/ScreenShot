package com.zxw.screenshot_master

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

/**
 * 利用ContentResolver监听照片数据的变化
 */
fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}