package com.lockminds.brass_services

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import com.lockminds.brass_services.Constants.Companion.LOGIN_STATUS
import com.lockminds.brass_services.Constants.Companion.PREFERENCE_KEY
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.geofence.GeofenceBroadcastReceiver
import com.lockminds.brass_services.geofence.createChannel
import com.lockminds.brass_services.model.Permissions
import com.lockminds.brass_services.reponses.LoginResponse
import com.lockminds.brass_services.ui.AccidentsActivity
import com.lockminds.brass_services.ui.AttendanceActivity
import com.lockminds.brass_services.viewmodel.PermissionsViewModel
import com.lockminds.brass_services.viewmodel.UserViewModel
import com.lockminds.libs.constants.APIURLs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val runningQOrLater =   Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private var cancellationTokenSource = CancellationTokenSource()
    private lateinit var locationCallback: LocationCallback
    private lateinit var geofencingClient: GeofencingClient

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private var mGeofenceList: ArrayList<Geofence>? = null

    private val locationRequest = LocationRequest.create()?.apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    protected var latitude = ""
    protected var longitude = ""
    protected var accuracy: Float = 0.0F
    lateinit var sessionManager: SessionManager
    lateinit var userViewModel: UserViewModel
    lateinit var permissionsViewModel: PermissionsViewModel
    lateinit var deviceId: String

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGeofenceList = ArrayList()
        geofencingClient = LocationServices.getGeofencingClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    accuracy = location.accuracy
                }
            }
        }

        // Create channel for notifications
        createChannel(this )

        sessionManager = SessionManager(applicationContext)

        userViewModel = ViewModelProvider(this,Injection.userViewModelFactory(this))
            .get(UserViewModel::class.java)
        permissionsViewModel = ViewModelProvider(this,Injection.permissionsViewModelFactory(this))
            .get(PermissionsViewModel::class.java)

        userViewModel.getUser(sessionManager.getUserId().toString()).observe(this){
            it?.let {user ->
                findViewById<TextView>(R.id.drawer_name)?.text = user.name
                findViewById<TextView>(R.id.drawer_email)?.text = user.reg_number
                val drawerImage = findViewById<ImageView>(R.id.drawer_image)
                val picture = findViewById<ImageView>(R.id.nav_icon)

                if(picture != null){
                    if(drawerImage != null){
                        Glide
                            .with(applicationContext)
                            .load(user.profile_photo_path)
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(picture)
                    }
                }

                if(drawerImage != null){
                    Glide
                        .with(applicationContext)
                        .load(user.profile_photo_path)
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .into(drawerImage)
                }
            }
        }

        deviceId = Settings.Secure.getString(applicationContext?.contentResolver, Settings.Secure.ANDROID_ID)

    }


    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setContentView(layoutResID)
        Tools.setSystemBarLight(this)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
    }

    override fun onPause() {
        super.onPause()
       // stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        syncProfile()
        checkLogin()
        initNavigation()
        syncPermissions()
        checkPermissions()
    }

    override fun onStart() {
        super.onStart()
        checkAppPermissions()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkAppPermissions() {
        // if (viewModel.geofenceIsActive()) return
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettings()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    /*
  *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
  */
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this@BaseActivity,
            permissionsArray,
            resultCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettings(false)
        }
    }

    /*
    *  Uses the Location Client to check the current state of location settings, and gives the user
    *  the opportunity to turn on location services within our app.
    */
    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->

            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@BaseActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    findViewById<TextView>(R.id.lyt_parent),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                startLocationUpdates()
            }
        }

    }

    /*
    *  Determines whether the app has the appropriate permissions across Android 10+ and all other
    *  Android versions.
    */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }


    private fun checkPermissions() {
        permissionsViewModel.getPermissions(sessionManager.getUserId().toString()).observe(this){
            it?.let { permissions ->
                findViewById<View>(R.id.accidents)?.isVisible = permissions.escort!! > 0
            }
        }
    }

    protected fun toolbarBackNavigation(){
        //actionbar
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_back)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun toast(message: String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(message: String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    protected fun syncProfile(){
        AndroidNetworking.post(APIURLs.BASE_URL + "sync_profile")
            .setPriority(Priority.HIGH)
            .build()
            .getAsParsed(
                object : TypeToken<LoginResponse?>() {},
                object : ParsedRequestListener<LoginResponse> {

                    override fun onResponse(response: LoginResponse) {

                        if (response.status) {

                            GlobalScope.launch {
                                (application as App).user.syncUser(response.user.user_id.toString(),response.user)
                            }

                            val preference = applicationContext?.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                                ?: return

                            with(preference.edit()) {
                                putString(Constants.NAME, response.name)
                                putString(Constants.PHONE_NUMBER, response.phone_number)
                                putString(Constants.PHOTO_URL, response.photo_url)
                                putString(Constants.USER_ID, response.id)
                                putString(Constants.EMAIL, response.email)
                                putString(Constants.POLICY_URL, response.policy_url)
                                putString(Constants.TEAM_EMAIL, response.team_email)
                                putString(Constants.TEAM_NAME, response.team_name)
                                putString(Constants.TEAM_ID, response.user.team_id)
                                putString(Constants.WAREHOUSE, response.user.warehouse_id)
                                putString(Constants.TEAM_NAME, response.team_name)
                                putString(Constants.TEAM_PHONE, response.team_phone)
                                putString(Constants.TEAM_ADDRESS, response.team_address)
                                putString(Constants.CHANGE_DETAILS, response.change_details)
                                putString(Constants.CHANGE_PICTURE, response.change_picture)
                                apply()
                            }
                        }

                    }

                    override fun onError(anError: ANError) {}

                })
    }

    fun syncPermissions(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_permissions")
            .setPriority(Priority.LOW)
            .build()
            .getAsObject(Permissions::class.java, object : ParsedRequestListener<Permissions> {
                override fun onResponse(permissions: Permissions) {
                    val permission: Permissions = permissions
                    GlobalScope.launch {
                        (application as App).permissions.syncPermissions(sessionManager.getUserId().toString(),permission)
                    }
                }
                override fun onError(anError: ANError) {

                }
            })
    }


    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    public override fun onStop() {
        super.onStop()
    }

    private fun checkLogin(){
        val preference = applicationContext?.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (preference != null) {
            if(preference.getString(LOGIN_STATUS,"false")  == "false"){
                applicationContext.cacheDir.deleteRecursively()
                val intent = Intent(applicationContext,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun restartActivity(){
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    fun showAppSetting(view: View){
        Snackbar.make(
            view,
            R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) {
                // Displays App settings screen.
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
    }

    fun openAppSettings(){
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun initNavigation() {

        findViewById<View>(R.id.accidents)?.setOnClickListener {
            val intent = Intent(this@BaseActivity, AccidentsActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.home)?.setOnClickListener {
            val intent = Intent(this@BaseActivity, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.attendance)?.setOnClickListener {
            val intent = Intent(this@BaseActivity, AttendanceActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.profile)?.setOnClickListener {
            val intent = Intent(this@BaseActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.logout)?.setOnClickListener {
            val preference = applicationContext?.getSharedPreferences(
                PREFERENCE_KEY,
                Context.MODE_PRIVATE
            )
            preference?.edit()?.clear()?.apply()
            clearAppData()
            val intent = Intent(this@BaseActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }



    }

    protected fun clearAppData() {
        try {
            GlobalScope.launch {
                AppDatabase.getDatabase(applicationContext,this).clearAllTables()
            }

            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
            } else {
                Runtime.getRuntime().exec("pm clear " + applicationContext.packageName)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    /*
        * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
        * the background permission as well.
        */
    /*
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            // Permission denied.
            Snackbar.make(
                findViewById<TextView>(R.id.lyt_parent),
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettings()
        }
    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "KELLY"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

