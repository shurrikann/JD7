package com.shurrikann.jd7.bean

import android.os.Parcel
import android.os.Parcelable

data class LocationData(
    var ch4Value: String,
    var cjTime: String,
    var coValue: String,
    var heatValue: String,
    var humValue: String,
    var infoId: String,
    var o2Value: String,
    var presValue: String,
    var speedValue: String,
    var quantityValue:String,//风量
    var areaValue:String,//面积
    var state: Int,//数据上传状态，0待上传，1在上传，2已上传
    var name: String,//上传的名字
    var upTime: String//上传时间
) : Parcelable {
    constructor(parcel: Parcel) : this(
        ch4Value = parcel.readString() ?: "",
        cjTime = parcel.readString() ?: "",
        coValue = parcel.readString() ?: "",
        heatValue = parcel.readString() ?: "",
        humValue = parcel.readString() ?: "",
        infoId = parcel.readString() ?: "",
        o2Value = parcel.readString() ?: "",
        presValue = parcel.readString() ?: "",
        speedValue = parcel.readString() ?: "",
        quantityValue = parcel.readString() ?: "",
        areaValue = parcel.readString() ?: "",
        state = parcel.readInt(),
        name = parcel.readString() ?: "",
        upTime = parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ch4Value)
        parcel.writeString(cjTime)
        parcel.writeString(coValue)
        parcel.writeString(heatValue)
        parcel.writeString(humValue)
        parcel.writeString(infoId)
        parcel.writeString(o2Value)
        parcel.writeString(presValue)
        parcel.writeString(speedValue)
        parcel.writeString(quantityValue)
        parcel.writeString(areaValue)
        parcel.writeInt(state)
        parcel.writeString(name)
        parcel.writeString(upTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationData> {
        override fun createFromParcel(parcel: Parcel): LocationData {
            return LocationData(parcel)
        }

        override fun newArray(size: Int): Array<LocationData?> {
            return arrayOfNulls(size)
        }
    }
}
