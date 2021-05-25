package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="users")
data class User(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val user_id: String?,
    val name: String?,
    val email: String?,
    val phone_number: String?,
    val address: String?,
    val reg_number: String?,
    val change_details: String?,
    val fcm_token: String?,
    val change_picture: String?,
    val email_verified_at: String?,
    val timezone: String?,
    val current_team_id: String?,
    val profile_photo_path: String?,
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
        "",
        "",
        "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(user_id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(phone_number)
        parcel.writeString(address)
        parcel.writeString(reg_number)
        parcel.writeString(change_details)
        parcel.writeString(fcm_token)
        parcel.writeString(change_picture)
        parcel.writeString(email_verified_at)
        parcel.writeString(timezone)
        parcel.writeString(current_team_id)
        parcel.writeString(profile_photo_path)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
