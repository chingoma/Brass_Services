package com.lockminds.brass_services.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="lots")
data class Lot(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val team_id: String?,
    val escort_list_id: String?,
    val source_id: String?,
    val source: String?,
    val driver: String?,
    val warehouse_id: String?,
    val destination_id: String?,
    val escorter_id: String?,
    val escorter: String?,
    val destination_status: String?,
    val lot_no: String?,
    val grade: String?,
    val no_bundles: String?,
    val pieces: String?,
    val mine_net_weight: String?,
    val mine_gross_weight: String?,
    val haulier: String?,
    val horse: String?,
    val trailer: String?,
    val container: String?,
    val wagon: String?,
    val loading_date: String?,
    val dispatch_date: String?,
    val check_in: String?,
    val check_out: String?,
    val action_description: String?,
    val action_id: String?,
    val late_check_out: String?,
    val check_out_weight: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val check_in_out_days: String?,
    val receiving_date: String?,
    val weight_change_reason: String?,
    val weight_change_qty: String?,
    val weight_change_type: String?,
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
        "",
        "",
        "",
        "",
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(team_id)
        parcel.writeString(escort_list_id)
        parcel.writeString(source_id)
        parcel.writeString(source)
        parcel.writeString(driver)
        parcel.writeString(warehouse_id)
        parcel.writeString(destination_id)
        parcel.writeString(escorter_id)
        parcel.writeString(escorter)
        parcel.writeString(destination_status)
        parcel.writeString(lot_no)
        parcel.writeString(grade)
        parcel.writeString(no_bundles)
        parcel.writeString(pieces)
        parcel.writeString(mine_net_weight)
        parcel.writeString(mine_gross_weight)
        parcel.writeString(haulier)
        parcel.writeString(horse)
        parcel.writeString(trailer)
        parcel.writeString(container)
        parcel.writeString(wagon)
        parcel.writeString(loading_date)
        parcel.writeString(dispatch_date)
        parcel.writeString(check_in)
        parcel.writeString(check_out)
        parcel.writeString(action_description)
        parcel.writeString(action_id)
        parcel.writeString(late_check_out)
        parcel.writeString(check_out_weight)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
        parcel.writeString(deleted_at)
        parcel.writeString(check_in_out_days)
        parcel.writeString(receiving_date)
        parcel.writeString(weight_change_reason)
        parcel.writeString(weight_change_qty)
        parcel.writeString(weight_change_type)
        parcel.writeString(synced)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Lot> {
        override fun createFromParcel(parcel: Parcel): Lot {
            return Lot(parcel)
        }

        override fun newArray(size: Int): Array<Lot?> {
            return arrayOfNulls(size)
        }
    }
}
