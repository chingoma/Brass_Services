package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="current_office")
data class CurrentOffice(
    @PrimaryKey(autoGenerate = false) var id: Long,
    var user_id: String?,
    var team_id: String?,
    var name: String?,
    var latitude: String?,
    var longitude: String?,
    var created_at: String?,
    var updated_at: String?,
    var deleted_at: String?,
    var synced: String?,
    var attendance: String?
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
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(user_id)
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

    companion object CREATOR : Parcelable.Creator<CurrentOffice> {
        override fun createFromParcel(parcel: Parcel): CurrentOffice {
            return CurrentOffice(parcel)
        }

        override fun newArray(size: Int): Array<CurrentOffice?> {
            return arrayOfNulls(size)
        }
    }
}
