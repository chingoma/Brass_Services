package com.lockminds.brass_services.retrofit

import com.google.gson.annotations.SerializedName
import com.lockminds.brass_services.model.Attendance

/**
 * Data class to hold repo responses from searchRepo API calls.
 */
data class AttendanceResponse(
        @SerializedName("total_count") val total: Int = 0,
        @SerializedName("items") val items: List<Attendance> = emptyList(),
        val nextPage: Int? = null
)
