package com.lockminds.brass_services.ui

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.SpinKitView
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.lockminds.brass_services.*
import com.lockminds.brass_services.adapter.AccidentGalleryAdapter
import com.lockminds.brass_services.databinding.ActivityAccidentBinding
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.model.AccidentGallery
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.viewmodel.AccidentGalleryViewModel
import com.lockminds.brass_services.viewmodel.AccidentGalleryViewModelFactory
import com.lockminds.brass_services.widget.SpacingItemDecoration
import com.lockminds.libs.constants.APIURLs
import com.lockminds.libs.constants.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.io.File

class AccidentActivity : BaseActivity() {

    private val accidentGalleryViewModel by viewModels<AccidentGalleryViewModel> {
        AccidentGalleryViewModelFactory((application as App).accidentGallery)
    }

    lateinit var binding: ActivityAccidentBinding
    private lateinit var accidentId: String
    private lateinit var accident: Accident
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccidentBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
        sessionManager = SessionManager(applicationContext)
        accidentId =  intent.getStringExtra(Constants.INTENT_PARAM_1)!!
        initComponents()
        GlobalScope.launch {
            val data = (application as App).accidents.getItem(accidentId)
            runOnUiThread {
                accident = data
                initActivity()
                binding.addPicture.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Glide
                .with(applicationContext)
                .load(sessionManager.getPhotoUrl())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.image)
                syncGallery()

    }

    private fun initActivity(){
        initAdapter()
        updateUi()
        syncGallery()
    }

    private fun updateUi(){
        binding.location.text = accident.location
        binding.description.text = accident.description
        binding.date.text = accident.accident_date
    }

    private fun initComponents() {
        binding.toolbar.navigationIcon = getDrawable(R.drawable.ic_back)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        binding.image.setOnClickListener {
            Dexter.withContext(this)
                    .withPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ).withListener(object : MultiplePermissionsListener {

                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(this@AccidentActivity);
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest?>?,
                                token: PermissionToken?
                        ) { /* ... */
                        }
                    }).check()
        }
        binding.name.text = sessionManager.getName()
        binding.contact.text = "Accident Details"

        binding.addPicture.setOnClickListener {
            Dexter.withContext(this)
                    .withPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ).withListener(object : MultiplePermissionsListener {

                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(this@AccidentActivity);
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest?>?,
                                token: PermissionToken?
                        ) { /* ... */
                        }
                    }).check()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_close, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home || item.itemId == R.id.action_close) {
            finish()
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initAdapter() {

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.addItemDecoration(SpacingItemDecoration(2, Tools.dpToPx(this, 3), true))
        binding.recyclerView.setHasFixedSize(true)

        val galleryAdapter = AccidentGalleryAdapter(this) { gallery -> galleryAdapterOnClickNear(
                gallery
        ) }

        accidentGalleryViewModel.allItems(accidentId).observe(this){
            it?.let {
                galleryAdapter.submitList(it)
            }
        }

        binding.recyclerView.adapter = galleryAdapter
    }

    private fun syncGallery(){
            AndroidNetworking.get(APIURLs.BASE_URL + "get_accident_galleries")
                    .addQueryParameter("id", accidentId)
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
                    .setPriority(Priority.HIGH)
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsObjectList(AccidentGallery::class.java, object : ParsedRequestListener<List<AccidentGallery>> {
                        override fun onResponse(list: List<AccidentGallery>) {
                            val items: List<AccidentGallery> = list
                            GlobalScope.launch {
                                (application as App).accidentGallery.syncItems(accidentId, items)
                            }
                        }

                        override fun onError(anError: ANError) {
                            Toast.makeText(this@AccidentActivity, anError.errorDetail, Toast.LENGTH_SHORT)
                                    .show()
                        }
                    })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                binding.spinKit.visibility = View.VISIBLE
                val resultUri: Uri = result!!.uri
                val file = File(resultUri.path)
                if(this::accident.isInitialized){
                    AndroidNetworking.upload(APIURLs.BASE_URL + "add_accident_gallery")
                            .addMultipartParameter("lot", accident.lot_no)
                            .addMultipartParameter("id", accidentId)
                            .addMultipartFile("photo", file)
                            .addHeaders("accept", "application/json")
                            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
                            .setPriority(Priority.HIGH)
                            .build()
                            .setUploadProgressListener { bytesUploaded, totalBytes ->
                                // do anything with progress
                            }
                            .getAsParsed(
                                    object : TypeToken<Response?>() {},
                                    object : ParsedRequestListener<Response> {

                                        override fun onResponse(response: Response) {
                                            binding.spinKit.visibility = View.GONE
                                            if (response.status) {
                                                syncGallery()
                                            }

                                            Toast.makeText(this@AccidentActivity, response.message, Toast.LENGTH_SHORT)
                                                    .show()
                                        }

                                        override fun onError(anError: ANError) {
                                            binding.spinKit.visibility = View.GONE
                                            Toast.makeText(this@AccidentActivity, anError.errorDetail, Toast.LENGTH_SHORT)
                                                    .show()
                                        }

                                    })
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error: Exception = result!!.error
            }
        }
    }
    private fun showImage(gallery: AccidentGallery) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_image)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        val image = dialog.findViewById<ImageView>(R.id.image)
        val date = dialog.findViewById<TextView>(R.id.date)
        val location = dialog.findViewById<TextView>(R.id.location)
        date.text = gallery.created_at
        location.text = gallery.lot_no
        Tools.displayImage(this@AccidentActivity,image,gallery.file)
        dialog.show()
    }


    private fun galleryAdapterOnClickNear(gallery: AccidentGallery) {
        showImage(gallery)
    }

    companion object {
        @JvmStatic
        fun createIntentAccident(context: Context, lot: String?): Intent {
            return Intent().setClass(context, AccidentActivity::class.java)
                    .putExtra(Constants.INTENT_PARAM_1, lot)
        }
    }

}