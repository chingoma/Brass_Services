package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="check_point_history")
data class CheckPointHistory(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val team_id: String?,
    val action_id: String?,
    val escort_list_data_id: String?,
    val escort_list_id: String?,
    val escorter: String?,
    val lot_no: String?,
    val destination_id: String?,
    val action_description: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val action: String?,
    val check_point: String?,
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
        "",
        "true"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(team_id)
        parcel.writeString(action_id)
        parcel.writeString(escort_list_data_id)
        parcel.writeString(escort_list_id)
        parcel.writeString(escorter)
        parcel.writeString(lot_no)
        parcel.writeString(destination_id)
        parcel.writeString(action_description)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
        parcel.writeString(check_point)
        parcel.writeString(action)
        parcel.writeString(synced)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheckPointHistory> {
        override fun createFromParcel(parcel: Parcel): CheckPointHistory {
            return CheckPointHistory(parcel)
        }

        override fun newArray(size: Int): Array<CheckPointHistory?> {
            return arrayOfNulls(size)
        }
    }
}
