package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="permissions")
data class Permissions(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val escort: Int?,
    val receive: Int?,
): Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    constructor(): this(
        1,
        0,
        0,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        if (escort != null) {
            parcel.writeInt(escort.toInt())
        }
        if (receive != null) {
            parcel.writeInt(receive.toInt())
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Permissions> {
        override fun createFromParcel(parcel: Parcel): Permissions {
            return Permissions(parcel)
        }

        override fun newArray(size: Int): Array<Permissions?> {
            return arrayOfNulls(size)
        }
    }
}
