package com.lockminds.brass_services.ui

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.lockminds.brass_services.*
import com.lockminds.brass_services.R
import com.lockminds.brass_services.adapter.AttendancePagedAdapter
import com.lockminds.brass_services.adapter.LoadStateAdapter
import com.lockminds.brass_services.adapter.OfficeAdapter
import com.lockminds.brass_services.databinding.ActivityAttendanceBinding
import com.lockminds.brass_services.geofence.*
import com.lockminds.brass_services.model.LandmarkDataObject
import com.lockminds.brass_services.model.Office
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.services.FetchAddressIntentService
import com.lockminds.brass_services.viewmodel.AttendancePagedViewModel
import com.lockminds.brass_services.viewmodel.OfficesViewModel
import com.lockminds.brass_services.viewmodel.OfficesViewModelFactory
import com.lockminds.libs.constants.APIURLs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class AttendanceActivity : BaseActivity(){

    private lateinit var viewModel: AttendancePagedViewModel
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

    private var mGeofenceList: ArrayList<Geofence>? = null
    private lateinit var geofencingClient: GeofencingClient
    private val ADDRESS_REQUESTED_KEY = "address-request-pending"
    private val LOCATION_ADDRESS_KEY = "location-address"
    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     */
    private var addressRequested = false

    /**
     * The formatted location address.
     */
    private var addressOutput = ""


    private var coordinate = ""

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private lateinit var resultReceiver: AddressResultReceiver


    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private var cancellationTokenSource = CancellationTokenSource()

    private val bReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if(intent.action == ACTION_GEOFENCE_EVENT){
                    enableActions(intent.getStringExtra("office").toString())
                    coordinate = intent.getStringExtra("coordinate").toString()
                }
                if(intent.action == ACTION_GEOFENCE_EVENT_EXIT){
                    disableControllers()
                    coordinate = ""
                }

            }
        }
    }

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

    private lateinit var binding: ActivityAttendanceBinding

    @ExperimentalPagingApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this, Injection.attendancePagedViewModelFactory(this))
            .get(AttendancePagedViewModel::class.java)

        resultReceiver = AddressResultReceiver(Handler())
        geofencingClient = LocationServices.getGeofencingClient(this)

        createChannel(this )

        initComponent()
        initNavigationMenu()
//        initMap()
        //updateValuesFromBundle(savedInstanceState)
        mGeofenceList = ArrayList()
        initAdapter()

    }

    @ExperimentalPagingApi
    private fun attemptCheckOut(){
        if(coordinate.isEmpty()){
            Toast.makeText(this@AttendanceActivity, resources.getText(R.string.coordinate_not_found,"coordinate not found"), Toast.LENGTH_SHORT).show()
            return
        }

        AndroidNetworking.post(APIURLs.BASE_URL + "check_out")
            .addBodyParameter("coordinate",coordinate)
            .addHeaders("accept", "application/json")
            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
            .setPriority(Priority.HIGH)
            .build()
            .getAsObject(Response::class.java, object : ParsedRequestListener<Response> {
                override fun onResponse(response: Response) {
                    val response: Response = response
                    if(response.status.equals(true)){
                        search(sessionManager.getUserId().toString())
                    }
                    Toast.makeText(this@AttendanceActivity, response.message, Toast.LENGTH_SHORT).show()
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@AttendanceActivity, anError.errorBody, Toast.LENGTH_SHORT)
                        .show()
                }
            })

    }

    @ExperimentalPagingApi
    private fun attemptCheckIn(){
        if(coordinate.isEmpty()){
            Toast.makeText(this@AttendanceActivity, resources.getText(R.string.coordinate_not_found,"coordinate not found"), Toast.LENGTH_SHORT).show()
            return
        }

        AndroidNetworking.post(APIURLs.BASE_URL + "check_in")
            .addBodyParameter("coordinate",coordinate)
            .addHeaders("accept", "application/json")
            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
            .setPriority(Priority.HIGH)
            .build()
            .getAsObject(Response::class.java, object : ParsedRequestListener<Response> {
                override fun onResponse(response: Response) {
                    val response: Response = response
                    if(response.status.equals(true)){
                        search(sessionManager.getUserId().toString())
                    }
                    Toast.makeText(this@AttendanceActivity, response.message, Toast.LENGTH_SHORT).show()
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@AttendanceActivity, anError.errorBody, Toast.LENGTH_SHORT)
                        .show()
                }
            })

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
        binding.status.text = Tools.fromHtml("Your location is <b>NOT ALLOWED</b>. Please go to Office")
        binding.currentLocation.text = Tools.fromHtml("Your current location is <b>UNKNOWN</b>")
        binding.startAttendance.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.checkIn.setOnClickListener {
            checkInDialog()
        }

        binding.checkOut.setOnClickListener {
            checkOutDialog()
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

    override fun onResume() {
        super.onResume()
        syncOffices()
        setAdapter()
        registerReceiver(bReceiver,   IntentFilter(ACTION_GEOFENCE_EVENT))
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
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
            if(extras.containsKey(GeofencingConstants.EXTRA_GEOFENCE_INDEX)){
                checkPermissionsAndStartGeofencing()
            }
        }
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

    private fun enableActions(office: String){
        //binding.currentLocation.isVisible = false
        binding.progressBar.isVisible = false
        binding.status.text = Tools.fromHtml("You have arrived at <b>${office}</b>")
        binding.checkIn.isVisible = true
        binding.checkOut.isVisible = true
        binding.intro.isVisible = true
    }
    /*
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
        unregisterReceiver(bReceiver)
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissionsAndStartGeofencing() {

        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }


    /**
     * Updates fields based on data stored in the bundle.
     */
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        ADDRESS_REQUESTED_KEY.let {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(it)) {
                addressRequested = savedInstanceState.getBoolean(it)
            }
        }

        LOCATION_ADDRESS_KEY.let {
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(it)) {
                addressOutput = savedInstanceState.getString(it).toString()
                displayAddressOutput()
            }
        }


    }

    /**
     * Updates the address in the UI.
     */
    private fun displayAddressOutput() {
        binding.currentLocation.text = addressOutput
    }


    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {

        binding.checkIn.isVisible = false
        binding.checkOut.isVisible = false
        binding.intro.isVisible = false

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->

            binding.checkIn.isVisible = false
            binding.checkOut.isVisible = false
            binding.intro.isVisible = false

            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@AttendanceActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {

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

        requestCurrentLocation()

        GlobalScope.launch {
            val offices = (application as App).offices.getList()
            runOnUiThread {
                offices.let{ list1 ->
                    if(list1.isNotEmpty()){
                        list1.forEach {
                            val lat = it.latitude.toString()
                            val long = it.longitude.toString()
                            val landMark = LandmarkDataObject(it.id.toString(), it.name.toString())
                            GeofencingConstants.LANDMARK_DATA.add(landMark)
                            // Build the Geofence Object
                            val geofence = Geofence.Builder()
                                // Set the request ID, string to identify the geofence.
                                .setRequestId(it.id.toString())
                                // Set the circular region of this geofence.
                                .setCircularRegion(lat.toDouble(),
                                    long.toDouble(),
                                    GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                                )
                                // Set the expiration duration of the geofence. This geofence gets
                                // automatically removed after this period of time.
                                .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                // Set the transition types of interest. Alerts are only generated for these
                                // transition. We track entry and exit transitions in this sample.
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .build()
                            mGeofenceList?.add(geofence)
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
                                        // Failed to add geofences.
                                        binding.checkIn.isVisible = false
                                        binding.checkOut.isVisible = false

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {

        binding.checkIn.isVisible = false
        binding.checkOut.isVisible = false

        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {

            addOnSuccessListener {}

            addOnFailureListener {}
        }
    }


    /**
     * Gets current location.
     * Note: The code checks for permission before calling this method, that is, it's never called
     * from a method with a missing permission. Also, I include a second check with my extension
     * function in case devs just copy/paste this code.
     */
    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {

        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Returns a single current location fix on the device. Unlike getLastLocation() that
            // returns a cached location, this method could cause active location computation on the
            // device. A single fresh location will be returned if the device location can be
            // determined within reasonable time (tens of seconds), otherwise null will be returned.
            //
            // Both arguments are required.
            // PRIORITY type is self-explanatory. (Other options are PRIORITY_BALANCED_POWER_ACCURACY,
            // PRIORITY_LOW_POWER, and PRIORITY_NO_POWER.)
            // The second parameter, [CancellationToken] allows the activity to cancel the request
            // before completion.
            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                val result = if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    "Your Location : ${result.latitude}, ${result.longitude}"

                } else {
                    val exception = task.exception
                    "Location UNKNOWN: $exception"
                }

                logOutputToScreen(result)

                if (task.isSuccessful && task.result != null){
                    val location: Location = task.result
                    // Create an intent for passing to the intent service responsible for fetching the address.
                    val intent = Intent(this, FetchAddressIntentService::class.java).apply {
                        // Pass the result receiver as an extra to the service.
                        putExtra(Constants.RECEIVER, resultReceiver)

                        // Pass the location data as an extra to the service.
                        putExtra(Constants.LOCATION_DATA_EXTRA, location)
                    }

                    // Start the service. If the service isn't already running, it is instantiated and started
                    // (creating a process for it if needed); if it is running then it remains running. The
                    // service kills itself automatically once all intents are processed.
                    startService(intent)
                }


            }
        }
    }

    private fun logOutputToScreen(outputString: String) {
        binding.currentLocation.text = outputString
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "com.lockminds.brass_services.geofence.action.ACTION_GEOFENCE_EVENT"
        internal const val ACTION_GEOFENCE_EVENT_EXIT =
            "com.lockminds.brass_services.geofence.action.ACTION_GEOFENCE_EVENT_EXIT"
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private inner class AddressResultReceiver internal constructor(
        handler: Handler
    ) : ResultReceiver(handler) {

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {

            // Display the address string or an error message sent from the intent service.
            addressOutput = resultData.getString(Constants.RESULT_DATA_KEY).toString()
            displayAddressOutput()

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            addressRequested = false
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        delayedIdle(1)
    }

    private var _idleHandler: Handler = Handler()
    private var _idleRunnable = Runnable {
        //handle your IDLE state
        binding.checkIn.isVisible = true
        binding.checkOut.isVisible = true
        binding.intro.isVisible = true
        binding.startAttendance.isVisible = false
    }

    override fun delayedIdle(delayMinutes: Long) {
        _idleHandler.removeCallbacks(_idleRunnable)
        _idleHandler.postDelayed(_idleRunnable, (delayMinutes))
    }

    private fun setAdapter() {

//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.setHasFixedSize(true)

        val officesAdapter = OfficeAdapter(this) { offices -> officeOnClickNear(
            offices
        ) }

        officesViewModel.allItems(sessionManager.getUserId().toString()).observe(this){

            it?.let {
                officesAdapter.submitList(it)
                if(it.isNotEmpty()){
                    binding.lytNoConnection.isVisible = false
                    checkPermissionsAndStartGeofencing()
                    binding.progressBar.isVisible = false
                }else{
                    disableControllers()
                    binding.progressBar.isVisible = true
                }
            }
        }

       // binding.recyclerView.adapter = officesAdapter

    }

    private fun officeOnClickNear(office: Office) {

    }

    private fun syncOffices(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_offices")
            .setTag("lots")
            .addHeaders("accept", "application/json")
            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
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
        binding.startAttendance.isVisible = false
    }

    private fun enableControllers(){
        binding.checkIn.isVisible = true
        binding.checkOut.isVisible = true
        binding.intro.isVisible = true
        binding.startAttendance.isVisible = false
    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1