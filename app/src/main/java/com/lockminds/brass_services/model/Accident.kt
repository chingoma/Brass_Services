package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="accidents")
data class Accident(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val team_id: String?,
    val lot_no: String?,
    val escorter_id: String?,
    val accident_date: String?,
    val driver: String?,
    val latitude: String?,
    val longitude: String?,
    val location: String?,
    val description: String?,
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
        "",
        "",
        "",
        "",
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(team_id)
        parcel.writeString(lot_no)
        parcel.writeString(escorter_id)
        parcel.writeString(accident_date)
        parcel.writeString(driver)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(description)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
        parcel.writeString(synced)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Accident> {
        override fun createFromParcel(parcel: Parcel): Accident {
            return Accident(parcel)
        }

        override fun newArray(size: Int): Array<Accident?> {
            return arrayOfNulls(size)
        }
    }
}
