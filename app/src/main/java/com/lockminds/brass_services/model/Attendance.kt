package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="attendances")
data class Attendance(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val team_id: String?,
    val name: String?,
    val office_id: String?,
    val coordinate_id: String?,
    val user_id: String?,
    val supervisor_id: String?,
    val attendance_date: String?,
    val time_in: String?,
    val time_out: String?,
    val working_hours: String?,
    val status: String?,
    val extra_hours: String?,
    val overtime_status: String?,
    val in_office: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?
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
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
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
        "",
        "",
        "",
        "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(team_id)
        parcel.writeString(name)
        parcel.writeString(office_id)
        parcel.writeString(coordinate_id)
        parcel.writeString(user_id)
        parcel.writeString(supervisor_id)
        parcel.writeString(attendance_date)
        parcel.writeString(time_in)
        parcel.writeString(time_out)
        parcel.writeString(working_hours)
        parcel.writeString(status)
        parcel.writeString(extra_hours)
        parcel.writeString(overtime_status)
        parcel.writeString(in_office)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Attendance> {
        override fun createFromParcel(parcel: Parcel): Attendance {
            return Attendance(parcel)
        }

        override fun newArray(size: Int): Array<Attendance?> {
            return arrayOfNulls(size)
        }
    }
}
