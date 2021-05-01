package com.lockminds.brass_services

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.androidnetworking.AndroidNetworking
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.database.repositories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class App: Application() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { AppDatabase.getDatabase(this,applicationScope) }
    val lotRepo by lazy { LotRepository(database.lotDao()) }
    val checkPoints by lazy { CheckPointRepository(database.checkPointDao()) }
    val checkPointsAction by lazy { CheckPointActionRepository(database.checkPointActionDao()) }
    val checkPointHistory by lazy { CheckPointHistoryRepository(database.checkPointHistoryDao()) }
    val accidents by lazy { AccidentRepository(database.accidentDao()) }
    val accidentGallery by lazy { AccidentGalleryRepository(database.accidentGalleryDao()) }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        MultiDex.install(this)
    }


    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)
        appContext = applicationContext
    }

    companion object {

        lateinit  var appContext: Context

    }

}