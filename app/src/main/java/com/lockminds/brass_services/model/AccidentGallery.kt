package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="accident_Galleries")
data class AccidentGallery(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val lot_no: String?,
    val accident_id: String?,
    val escorter: String?,
    val file: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val synced: String?
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(lot_no)
        parcel.writeString(accident_id)
        parcel.writeString(escorter)
        parcel.writeString(file)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
        parcel.writeString(synced)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccidentGallery> {
        override fun createFromParcel(parcel: Parcel): AccidentGallery {
            return AccidentGallery(parcel)
        }

        override fun newArray(size: Int): Array<AccidentGallery?> {
            return arrayOfNulls(size)
        }
    }


}
