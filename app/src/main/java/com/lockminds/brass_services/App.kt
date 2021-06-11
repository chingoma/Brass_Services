package com.lockminds.brass_services

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.multidex.MultiDex
import com.androidnetworking.AndroidNetworking
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.database.repositories.*
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class App: Application() {

    lateinit var deviceId: String
    lateinit var sessionManager: SessionManager

    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { AppDatabase.getDatabase(this,applicationScope) }
    val lotRepo by lazy { LotRepository(database.lotDao()) }
    val reasons by lazy { ReasonsRepository(database.reasonDao()) }
    val checkPoints by lazy { CheckPointRepository(database.checkPointDao()) }
    val checkPointsAction by lazy { CheckPointActionRepository(database.checkPointActionDao()) }
    val checkPointHistory by lazy { CheckPointHistoryRepository(database.checkPointHistoryDao()) }
    val accidents by lazy { AccidentRepository(database.accidentDao()) }
    val attendances by lazy { AttendanceRepository(database.attendanceDao()) }
    val accidentGallery by lazy { AccidentGalleryRepository(database.accidentGalleryDao()) }
    val offices by lazy { OfficeRepository(database.officeDao()) }
    val currentOffice by lazy { CurrentOfficeRepository(database.currentOfficeDao()) }
    val user by lazy { UserRepository(database.userDao()) }
    val permissions by lazy { PermissionsRepository(database.permissionsDao()) }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        MultiDex.install(this)
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        deviceId = Settings.Secure.getString(applicationContext?.contentResolver, Settings.Secure.ANDROID_ID)
        sessionManager = SessionManager(this)
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.request().newBuilder()
                    .addHeader("accept", "application/json")
                    .addHeader("device", deviceId)
                    .addHeader("ip", Tools.getLocalIpAddress())
                    .addHeader("user-agent", "Lockminds-Trucks-Client-Android")
                    .addHeader("Authorization", "Bearer " + sessionManager.getLoginToken())
                    .build()
                    .let(chain::proceed)
            }
            .build()

        AndroidNetworking.initialize(applicationContext, okHttpClient)
        appContext = applicationContext
       // initPusher()
    }

    private fun initPusher() {

        val options = PusherOptions()
        options.setCluster("mt1")
        val pusher = Pusher("61b4c0df96f65b44b4e4", options)
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.i(
                    "Pusher",
                    "State changed from ${change.previousState} to ${change.currentState}"
                )
            }

            override fun onError(
                message: String,
                code: String,
                e: Exception
            ) {
                Log.i(
                    "Pusher",
                    "There was a problem connecting! code ($code), message ($message), exception($e)"
                )
            }
        }, ConnectionState.ALL)

        val channel = pusher.subscribe("my-channel")
        channel.bind("my-event") { event ->
            Log.i("Pusher", "Received event with data: $event")

        }
    }

    companion object {

        lateinit  var appContext: Context

    }

}