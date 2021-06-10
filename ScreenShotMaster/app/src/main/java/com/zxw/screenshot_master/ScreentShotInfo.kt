package com.zxw.screenshot_master

import android.os.Parcel
import android.os.Parcelable

/**
 * 遍历本地图片，保存数据到这个类中
 */
class ScreentShotInfo(
    var name: String? = null,
    var path: String? = null,
    var size: Long = 0,
    var width: Int = 0,
    var height: Int = 0,
    var mineType: String? = null,
    var addTime: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeLong(size)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeString(mineType)
        parcel.writeLong(addTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScreentShotInfo> {
        override fun createFromParcel(parcel: Parcel): ScreentShotInfo {
            return ScreentShotInfo(parcel)
        }

        override fun newArray(size: Int): Array<ScreentShotInfo?> {
            return arrayOfNulls(size)
        }
    }
}