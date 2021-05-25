package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="offices")
data class Office(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val team_id: String?,
    val name: String?,
    val latitude: String?,
    val longitude: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val synced: String?,
    val attendance: String?
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
        parcel.readString(),
        parcel.readString()
    ) {
    }

    constructor(): this(
        1,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(team_id)
        parcel.writeString(name)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
        parcel.writeString(synced)
        parcel.writeString(attendance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Office> {
        override fun createFromParcel(parcel: Parcel): Office {
            return Office(parcel)
        }

        override fun newArray(size: Int): Array<Office?> {
            return arrayOfNulls(size)
        }
    }
}
