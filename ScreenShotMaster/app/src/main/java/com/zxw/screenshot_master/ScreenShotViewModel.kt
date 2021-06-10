package com.zxw.screenshot_master

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


/**
 *  查询截图图片
 */
class ScreenShotViewModel : ViewModel() {

    companion object {
        const val TAG = "ScreenShotViewModel"
    }

    //查询的照片类型
    private val imageType = arrayOf("image/png", "image/jpeg")

    /**
     * 查询截屏照片的数据类型枚举
     */
    private val ScreenShotProjection = arrayOf( //查询图片需要的数据列
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,  //图片的显示名称  aaa.jpg
        MediaStore.Images.Media.DATA,  //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
        MediaStore.Images.Media.SIZE,  //图片的大小，long型  132492
        MediaStore.Images.Media.WIDTH,  //图片的宽度，int型  1s920
        MediaStore.Images.Media.HEIGHT,  //图片的高度，int型  1080
        MediaStore.Images.Media.MIME_TYPE,  //图片的类型     image/jpeg
        MediaStore.Images.Media.DATE_ADDED //图片被添加的时间，long型  1450518608
    )

    /**
     * 判断图片的路径 是否截屏
     * 因手机厂商不同 所以截图路径不同
     * 大而全的方案
     */
    private val screenShoot = arrayOf(
        "screenshot", "screen_shot", "screen-sh", "screen shot",
        "screencapture", "screen_capture ", "screen-capture", "screen capture",
        "screencap", "screen_cap", "screen-cap", "screen cap"
    )

    //监听到截图后通过LiveData通知到view层
    val _dataChanged = MutableLiveData<Boolean>()
    val dataChanged: LiveData<Boolean>
        get() = _dataChanged

    //保存截图照片的数据
    private val _screentShotInfoData = MutableLiveData<ScreentShotInfo>()
    val screentShotInfoData: LiveData<ScreentShotInfo>
        get() = _screentShotInfoData

    //照片数据监听对象
    private var contentObserver: ContentObserver? = null

    fun registerContentObserver() {
        if (contentObserver == null) {
            contentObserver =
                ScreenShotApplication.applicationContext.contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {
                    //监听到截图后
                    _dataChanged.value = true
                }
        }
    }

    //获取截图图片
    fun getLatestImage(bucketId: String? = null) {
        Thread {
            try {
                var data: ScreentShotInfo? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    data = queryImagesP(bucketId)
                } else {
                    data = queryImages(bucketId)
                }
                val imagePath = data.path?.toLowerCase()
                screenShoot.forEach {
                    if (imagePath?.contains(it)!! && (System.currentTimeMillis() / 1000 - data.addTime < 2)) {
                        _screentShotInfoData.postValue(data)
                        return@forEach
                    }
                }
            } catch (e: Exception) {
            }
        }.start()
    }

    /**
     * 只获取普通图片，不获取Gif
     */
    fun queryImages(bucketId: String?): ScreentShotInfo {
        val screentShotInfo = ScreentShotInfo()

        val uri = MediaStore.Files.getContentUri("external")
        val sortOrder = MediaStore.Images.Media._ID + " DESC limit 1 "
        var selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) +
                " AND " + MediaStore.Images.Media.MIME_TYPE + "=?" +
                " or " + MediaStore.Images.Media.MIME_TYPE + "=?"
        try {
            val data = ScreenShotApplication.applicationContext.contentResolver.query(
                uri,
                ScreenShotProjection,
                selection,
                imageType,
                sortOrder
            )

            if (data == null) {
                return screentShotInfo
            }

            if (data.moveToFirst()) {
                //查询数据
                val imageId: String =
                    data.getString(data.getColumnIndexOrThrow(ScreenShotProjection[0]))
                val imagePath: String =
                    data.getString(data.getColumnIndexOrThrow(ScreenShotProjection[1]))
                val imageSize: Long =
                    data.getLong(data.getColumnIndexOrThrow(ScreenShotProjection[2]))
                val imageWidth: Int =
                    data.getInt(data.getColumnIndexOrThrow(ScreenShotProjection[3]))
                val imageHeight: Int =
                    data.getInt(data.getColumnIndexOrThrow(ScreenShotProjection[4]))
                val imageMimeType: String =
                    data.getString(data.getColumnIndexOrThrow(ScreenShotProjection[5]))
                val imageAddTime: Long =
                    data.getLong(data.getColumnIndexOrThrow(ScreenShotProjection[6]))
                screentShotInfo.path = imagePath
                screentShotInfo.addTime = imageAddTime
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return screentShotInfo
    }

    /**
     * 只获取普通图片，不获取Gif（在Android11的机器中）
     * 在targetSdkVersion适配到30后  查询图片的Sql发生了变化
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @WorkerThread
    fun queryImagesP(bucketId: String?): ScreentShotInfo {
        val screentShotInfo = ScreentShotInfo()
        val uri = MediaStore.Files.getContentUri("external")
        val sortOrder = MediaStore.Files.FileColumns._ID + " DESC"
        var selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) +
                " AND " + MediaStore.Images.Media.MIME_TYPE + "=?" +
                " or " + MediaStore.Images.Media.MIME_TYPE + "=?"

        val bundle = createSqlQueryBundle(selection, imageType, sortOrder, 1)

        try {
            val data = ScreenShotApplication.applicationContext.contentResolver.query(
                uri,
                ScreenShotProjection,
                bundle,
                null
            )

            if (data == null) {
                return screentShotInfo
            }

            if (data.moveToFirst()) {
                //查询数据
                val imageId: String =
                    data.getString(data.getColumnIndexOrThrow(ScreenShotProjection[0]))
                val imagePath: String =
                    data.getString(data.getColumnIndexOrThrow(ScreenShotProjection[1]))
                val imageSize: Long =
                    data.getLong(data.getColumnIndexOrThrow(ScreenShotProjection[2]))
                val imageWidth: Int =
                    data.getInt(data.getColumnIndexOrThrow(ScreenShotProjection[3]))
                val imageHeight: Int =
                    data.getInt(data.getColumnIndexOrThrow(ScreenShotProjection[4]))
                val imageMimeType: String =
                    data.getString(data.getColumnIndexOrThrow(ScreenShotProjection[5]))
                val imageAddTime: Long =
                    data.getLong(data.getColumnIndexOrThrow(ScreenShotProjection[6]))
                screentShotInfo.path = imagePath
                screentShotInfo.addTime = imageAddTime
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return screentShotInfo
    }

    /*
    * 创建Android11 所需要的bundle对象
    * */
    fun createSqlQueryBundle(
        selection: String,
        selectionArgs: Array<String>,
        sortOrder: String?, limitCount: Int = 0, offset: Int = 0
    ): Bundle? {
        if (selection == null && selectionArgs == null && sortOrder == null) {
            return null
        }
        val queryArgs = Bundle()
        if (selection != null) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
        }
        if (selectionArgs != null) {
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        }
        if (sortOrder != null) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
        }
        queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, "$limitCount offset $offset")
        return queryArgs
    }

    /*
    * 注销观察者
    * */
    override fun onCleared() {
        contentObserver?.let {
            ScreenShotApplication.applicationContext.contentResolver.unregisterContentObserver(it)
        }
    }
}


