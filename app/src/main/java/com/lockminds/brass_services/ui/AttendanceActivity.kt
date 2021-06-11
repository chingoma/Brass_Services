/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lockminds.brass_services.ui

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.example.android.location.currentlocationkotlin.hasPermission
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.lockminds.brass_services.*
import com.lockminds.brass_services.Constants.Companion.ACTION_GEOFENCE_EVENT
import com.lockminds.brass_services.Constants.Companion.LOCKMINDS_ACTION_GEOFENCE_EVENT
import com.lockminds.brass_services.Constants.Companion.LOCKMINDS_ACTION_GEOFENCE_EVENT_ENTERING
import com.lockminds.brass_services.Constants.Companion.LOCKMINDS_ACTION_GEOFENCE_EVENT_EXIT
import com.lockminds.brass_services.R
import com.lockminds.brass_services.adapter.AttendancePagedAdapter
import com.lockminds.brass_services.adapter.LoadStateAdapter
import com.lockminds.brass_services.adapter.OfficeAdapter
import com.lockminds.brass_services.databinding.ActivityAttendanceBinding
import com.lockminds.brass_services.geofence.*
import com.lockminds.brass_services.model.CurrentOffice
import com.lockminds.brass_services.model.Office
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.viewmodel.AttendancePagedViewModel
import com.lockminds.brass_services.viewmodel.CurrentOfficeViewModel
import com.lockminds.brass_services.viewmodel.OfficesViewModel
import com.lockminds.brass_services.viewmodel.OfficesViewModelFactory
import com.lockminds.libs.constants.APIURLs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



class AttendanceActivity : BaseActivity() {

    private var receiver: BroadcastReceiver? = null

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var geofencingClient: GeofencingClient

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private lateinit var viewModel: AttendancePagedViewModel
    private lateinit var currentOfficeModel: CurrentOfficeViewModel
    private val adapter = AttendancePagedAdapter()
    private var searchJob: Job? = null

    @ExperimentalPagingApi
    private fun search(query: String) {
        // Make sure we cancel the previous job before creating a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.getItems(applicationContext,query).collectLatest {
                adapter.submitData(it)
            }
        }
    }


    private val officesViewModel by viewModels<OfficesViewModel> {
        OfficesViewModelFactory((application as App).offices)
    }
    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private var cancellationTokenSource = CancellationTokenSource()

    /**
     * The list of geofences used in this sample.
     */
    private var mGeofenceList: ArrayList<Geofence>? = null

    private lateinit var currentOffice: CurrentOffice

    @ExperimentalPagingApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        currentOffice = CurrentOffice()
        mGeofenceList = ArrayList()
        geofencingClient = LocationServices.getGeofencingClient(this)

        // Create channel for notifications
        createChannel(this )

        viewModel = ViewModelProvider(this, Injection.attendancePagedViewModelFactory(this))
            .get(AttendancePagedViewModel::class.java)

        currentOfficeModel = ViewModelProvider(this, Injection.currentOfficeModelFactory(this))
            .get(CurrentOfficeViewModel::class.java)

        initComponent()
        initNavigationMenu()
        initAdapter()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {

                    if (intent.action == LOCKMINDS_ACTION_GEOFENCE_EVENT)  {

                        val fenceId = intent.getStringExtra("fenceID")
                        val action = intent.getStringExtra("track_action")

                        if(action == LOCKMINDS_ACTION_GEOFENCE_EVENT_ENTERING){
                            GlobalScope.launch {
                                    val office = officesViewModel.getOffice(fenceId.toString())
                                    currentOffice.user_id = sessionManager.getUserId()
                                    currentOffice.attendance = office.attendance
                                    currentOffice.created_at = office.created_at
                                    currentOffice.deleted_at = office.deleted_at
                                    currentOffice.id = office.id
                                    currentOffice.latitude = office.latitude
                                    currentOffice.longitude = office.longitude
                                    currentOffice.name = office.name
                                    currentOffice.synced = office.synced
                                    currentOffice.team_id = office.team_id
                                    currentOffice.updated_at = office.updated_at
                                    val list = arrayListOf<CurrentOffice>()
                                    list.add(currentOffice)

                                    (application as App).currentOffice.syncItems(currentOffice.user_id.toString(),list)

                                    runOnUiThread {
                                        currentOffice.name?.let {
                                            enableActions()
                                        }
                                    }
                                }
                        }

                        if(action == LOCKMINDS_ACTION_GEOFENCE_EVENT_EXIT){
                            toast("ametoka")
                        }

                    }
                }
            }
        }

        registerReceiver(receiver,   IntentFilter(LOCKMINDS_ACTION_GEOFENCE_EVENT))
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    @SuppressLint("MissingPermission")
    @ExperimentalPagingApi
    private fun attemptCheckOut(){
        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    currentOffice.name?.let {
                        binding.loader.isVisible = true
                        AndroidNetworking.post(APIURLs.BASE_URL + "check_out")
                            .addBodyParameter("coordinate", currentOffice.id.toString())
                            .addBodyParameter("latitude", result.latitude.toString())
                            .addBodyParameter("longitude", result.longitude.toString())
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsObject(
                                Response::class.java,
                                object : ParsedRequestListener<Response> {
                                    override fun onResponse(response: Response) {
                                        binding.loader.isVisible = false
                                        val response: Response = response
                                        if (response.status.equals(true)) {

                                            search(sessionManager.getUserId().toString())
                                        }
                                        Toast.makeText(
                                            this@AttendanceActivity,
                                            response.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(anError: ANError) {
                                        binding.loader.isVisible = false
                                    }
                                })
                    }
                }
            }
        }

    }

    @SuppressLint("HardwareIds", "MissingPermission")
    @ExperimentalPagingApi
    private fun attemptAttendanceRequest(location: String){

        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    binding.loader.isVisible = true
                    AndroidNetworking.post(APIURLs.BASE_URL + "request_coordinate")
                        .addBodyParameter("location",location)
                        .addBodyParameter("latitude",result.latitude.toString())
                        .addBodyParameter("longitude",result.longitude.toString())
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsObject(Response::class.java, object : ParsedRequestListener<Response> {

                            override fun onResponse(response: Response) {
                                binding.loader.isVisible = false
                                val response: Response = response
                                if(response.status.equals(true)){
                                    search(sessionManager.getUserId().toString())
                                }
                                Toast.makeText(this@AttendanceActivity, response.message, Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(anError: ANError) {
                                binding.loader.isVisible = false
                                Toast.makeText(this@AttendanceActivity, anError.errorBody, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })

                } else {
                    Toast.makeText(this@AttendanceActivity, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    restartActivity()
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    @ExperimentalPagingApi
    private fun attemptCheckIn(){
        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    currentOffice.name?.let {
                        binding.loader.isVisible = true
                        AndroidNetworking.post(APIURLs.BASE_URL + "check_in")
                            .addBodyParameter("coordinate",currentOffice.id.toString())
                            .addBodyParameter("latitude",result.latitude.toString())
                            .addBodyParameter("longitude",result.longitude.toString())
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsObject(Response::class.java, object : ParsedRequestListener<Response> {
                                override fun onResponse(response: Response) {
                                    binding.loader.isVisible = false
                                    val response: Response = response
                                    if(response.status.equals(true)){

                                        search(sessionManager.getUserId().toString())
                                    }
                                    Toast.makeText(this@AttendanceActivity, response.message, Toast.LENGTH_SHORT).show()
                                }

                                override fun onError(anError: ANError) {
                                    binding.loader.isVisible = false
                                }
                            })
                    }
                }
            }
        }

    }

    @ExperimentalPagingApi
    private fun initAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            footer = LoadStateAdapter { adapter.retry() }
        )
        adapter.addLoadStateListener { loadState ->
            // Only show the list if refresh succeeds.
            binding.recyclerView.isVisible = true
            //loadState.source.refresh is LoadState.NotLoading
            // Show loading spinner during initial load or refresh.
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            // Show the retry state if initial load or refresh fails.
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {

            }
        }

        search(sessionManager.getUserId().toString())
    }

    @ExperimentalPagingApi
    private fun initComponent() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        binding.toolbar.title = "Attendance"
        binding.toolbar.subtitle = "check in or check out"
        binding.status.text = Tools.fromHtml("You may request your supervisor to allow <b>CHECK IN/OUT</b> from here. click <b>SUBMIT REQUEST</b> to send request")
        binding.currentLocation.text = Tools.fromHtml("<b>SUBMIT REQUEST</b>")

        binding.checkIn.setOnClickListener {
            checkInDialog()
        }

        binding.checkOut.setOnClickListener {
            checkOutDialog()
        }

        binding.currentLocation.setOnClickListener {
            requestCoordinateDialog()
        }

        binding.requestsCard.setOnClickListener {
            openAppSettings()
        }

    }

    private fun initNavigationMenu() {
        val drawer = binding.drawerLayout
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
        }

        drawer.addDrawerListener(toggle)

        // open/close drawer at start
        binding.navIcon.setOnClickListener{
            drawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this@AttendanceActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    @ExperimentalPagingApi
    private fun checkInDialog() {
        val builder = AlertDialog.Builder(this,R.style.dialogs)
        builder.setTitle("PERFORM CHECK IN ?")
        builder.setMessage("Be carefully as you can not undo this action")
        builder.setPositiveButton("CHECK IN",
            DialogInterface.OnClickListener { _, i ->
                attemptCheckIn()
            })
        builder.setNegativeButton("NOT NOW", null)
        builder.show()
    }

    @ExperimentalPagingApi
    private fun requestCoordinateDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before

        dialog.setContentView(R.layout.request_coordinate_dialog)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val location = dialog.findViewById<EditText>(R.id.location)

        (dialog.findViewById<View>(R.id.bt_close) as ImageButton).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.bt_save) as Button).setOnClickListener {
            attemptAttendanceRequest(location.text.toString())
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.attributes = lp

    }

    @ExperimentalPagingApi
    private fun checkOutDialog() {
        val builder = AlertDialog.Builder(this, R.style.dialogs)
        builder.setTitle("PERFORM CHECK OUT ?")
        builder.setMessage("Be carefully as you can not undo this action")
        builder.setPositiveButton("CHECK OUT",
            DialogInterface.OnClickListener { _, i ->
                attemptCheckOut()
            })
        builder.setNegativeButton("NOT NOW", null)
        builder.show()
    }

    private fun enableActions(){
        binding.progressBar.isVisible = false
        binding.status.isVisible = false
        binding.checkIn.isVisible = true
        binding.checkOut.isVisible = true
        binding.intro.isVisible = true
        binding.currentLocation.isVisible = false
    }


    @SuppressLint("Range", "MissingPermission")
    private fun setAdapter() {

        syncOffices()

        val officesAdapter = OfficeAdapter(this) { offices -> officeOnClickNear(
            offices
        ) }

        officesViewModel.allItems(sessionManager.getUserId().toString()).observe(this){

            it?.let {
                officesAdapter.submitList(it)
                if(it.isNotEmpty()){
                    it.forEach { office ->
                        mGeofenceList!!.add(
                            Geofence.Builder() // Set the request ID of the geofence. This is a string to identify this
                                // geofence.
                                .setRequestId(office.id.toString()) // Set the circular region of this geofence.
                                .setCircularRegion(
                                    office.latitude!!.toDouble(),
                                    office.longitude!!.toDouble(),
                                    Constants.GEOFENCE_RADIUS_IN_METERS
                                )
                                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                // Set the transition types of interest. Alerts are only generated for these
                                // transition. We track entry and exit transitions in this sample.
                                .setTransitionTypes(
                                    Geofence.GEOFENCE_TRANSITION_ENTER or
                                            Geofence.GEOFENCE_TRANSITION_EXIT
                                ) // Create the geofence.
                                .build()
                        )
                    }

                    // Build the geofence request
                    val geofencingRequest = GeofencingRequest.Builder()
                        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
                        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
                        // is already inside that geofence.
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

                        // Add the geofences to be monitored by geofencing service.
                        .addGeofences(mGeofenceList)
                        .build()

                    // First, remove any existing geofences that use our pending intent
                    geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                        // Regardless of success/failure of the removal, add the new geofence
                        addOnCompleteListener {
                            // Add the new geofence request with the new geofence
                            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                                addOnSuccessListener {
                                    // Geofences added.
                                }
                                addOnFailureListener {

                                }
                            }
                        }
                    }

                    binding.lytNoConnection.isVisible = false
                    binding.progressBar.isVisible = false
                }else{
                    disableControllers()
                    binding.progressBar.isVisible = true
                }
            }
        }

    }

    private fun officeOnClickNear(office: Office) {

    }

    private fun syncOffices(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_offices")
            .setPriority(Priority.HIGH)
            .setPriority(Priority.LOW)
            .build()
            .getAsObjectList(Office::class.java, object : ParsedRequestListener<List<Office>> {
                override fun onResponse(list: List<Office>) {
                    val items: List<Office> = list
                    GlobalScope.launch {
                        (application as App).offices.syncItems(items)
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@AttendanceActivity, anError.errorDetail, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun disableControllers(){
        binding.checkIn.isVisible = false
        binding.checkOut.isVisible = false
        binding.intro.isVisible = false
        binding.status.isVisible = true
        binding.currentLocation.isVisible = true
    }

    /*
 *  When we get the result from asking the user to turn on device location, we call
 *  checkDeviceLocationSettingsAndStartGeofence again to make sure it's actually on, but
 *  we don't resolve the check to keep the user from seeing an endless loop.
 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    /*
     *  When the user clicks on the notification, this method will be called, letting us know that
     *  the geofence has been triggered, and it's time to move to the next one in the treasure
     *  hunt.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent?.extras
        if(extras != null){

        }
    }

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
                binding.activityMapsMain,
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
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    /**
     * This will also destroy any saved state in the associated ViewModel, so we remove the
     * geofences here.
     */
    override fun onDestroy() {
        super.onDestroy()
        removeGeofences()
        unregisterReceiver(receiver)
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissionsAndStartGeofencing() {
       // if (viewModel.geofenceIsActive()) return
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
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
                    exception.startResolutionForResult(this@AttendanceActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.activityMapsMain,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
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
            this@AttendanceActivity,
            permissionsArray,
            resultCode
        )
    }

    /*
     * Adds a Geofence for the current clue if needed, and removes any existing Geofence. This
     * method should be called after the user has granted the location permission.  If there are
     * no more geofences, we remove the geofence and let the viewmodel know that the ending hint
     * is now "active."
     */
    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue() {
        setAdapter()
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences removed
                Log.d(TAG, getString(R.string.geofences_removed))
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "AttendanceActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1