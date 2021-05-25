package com.lockminds.brass_services.retrofit

import com.lockminds.libs.constants.APIURLs.Companion.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query


/**
 * Order API communication setup via Retrofit.
 */
interface AttendanceService {

    /**
     * Get order ordered by id desc.
     */
    @GET("get_attendance")
    suspend fun getAttendance(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") itemsPerPage: Int
    ): AttendanceResponse


    companion object {

        fun create(): AttendanceService {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AttendanceService::class.java)
        }
    }
}