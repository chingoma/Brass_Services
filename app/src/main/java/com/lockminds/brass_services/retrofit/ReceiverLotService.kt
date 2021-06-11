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
interface ReceiverLotService {

    /**
     * Get order ordered by id desc.
     */
    @GET("receiver/lots")
    suspend fun getLots(
        @Header("device") device: String,
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("warehouse") warehouse: String,
        @Query("page") page: Int,
        @Query("per_page") itemsPerPage: Int
    ): ReceiverLotResponse


    companion object {

        fun create(): ReceiverLotService {
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
                    .create(ReceiverLotService::class.java)
        }
    }
}