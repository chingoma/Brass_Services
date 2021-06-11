package com.lockminds.brass_services.retrofit

import com.google.gson.annotations.SerializedName
import com.lockminds.brass_services.model.Attendance
import com.lockminds.brass_services.model.ReceiverLot

/**
 * Data class to hold repo responses from searchRepo API calls.
 */
data class ReceiverLotResponse(
        @SerializedName("total_count") val total: Int = 0,
        @SerializedName("items") val items: List<ReceiverLot> = emptyList(),
        val nextPage: Int? = null
)
