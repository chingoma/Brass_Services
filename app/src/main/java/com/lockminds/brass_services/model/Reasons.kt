package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="reasons")
data class Reasons(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val team_id: String?,
    val name: String?,
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
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(team_id)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
        parcel.writeString(synced)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reasons> {
        override fun createFromParcel(parcel: Parcel): Reasons {
            return Reasons(parcel)
        }

        override fun newArray(size: Int): Array<Reasons?> {
            return arrayOfNulls(size)
        }
    }
}
