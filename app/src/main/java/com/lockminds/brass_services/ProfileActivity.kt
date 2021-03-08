package com.lockminds.brass_services

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.lockminds.brass_services.databinding.ActivityProfileBinding
import com.lockminds.brass_services.reponses.Response
import com.lockminds.libs.constants.APIURLs
import com.lockminds.libs.constants.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.json.JSONObject
import java.io.File


class ProfileActivity : BaseActivity() {

    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        initComponents()
        initInformation()
    }

    override fun onResume() {
        super.onResume()
        val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (preference != null) {

            Glide
                .with(applicationContext)
                .load(preference.getString(Constants.PHOTO_URL, ""))
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.image)
        }
    }
    private fun initComponents() {
        binding.toolbar.navigationIcon = getDrawable(R.drawable.ic_back)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)

        binding.logout.setOnClickListener {
            val preference = applicationContext?.getSharedPreferences(
                    Constants.PREFERENCE_KEY,
                    Context.MODE_PRIVATE
            )
            preference?.edit()?.clear()?.commit()
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.aboutTeam.setOnClickListener {
            showAboutApp()
        }

        binding.privacyPolicy.setOnClickListener {
            loadWebFromUrl()
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


    fun initInformation(){
        val preference = applicationContext?.getSharedPreferences(
                Constants.PREFERENCE_KEY,
                Context.MODE_PRIVATE
        )
        if (preference != null) {
            binding.name.text = preference.getString(
                    Constants.NAME,
                    resources.getString(R.string.app_name)
            )
            binding.name1.text = preference.getString(
                    Constants.NAME,
                    resources.getString(R.string.app_name)
            )
            binding.phoneNumber.text = preference.getString(Constants.PHONE_NUMBER, "")
            Glide
                .with(applicationContext)
                .load(preference.getString(Constants.PHOTO_URL, ""))
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.image)
            binding.contact.text = preference.getString(Constants.EMAIL, "info@brassservices.co.tz")

            val change_details = preference.getString(Constants.CHANGE_DETAILS, "null");
            if (change_details.equals("1")){
                binding.fab.isVisible = true
            }

            binding.fab.setOnClickListener {
                val intent = Intent(this@ProfileActivity, UpdateProfileActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.changePassword.setOnClickListener {
                val intent = Intent(this@ProfileActivity, ChangePasswordActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.image.setOnClickListener {
                val change_picture = preference.getString(Constants.CHANGE_PICTURE, "null");
                if (change_picture.equals("1")){
                    Dexter.withContext(this)
                        .withPermissions(
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(object : MultiplePermissionsListener {

                                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .start(this@ProfileActivity);
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                        permissions: List<PermissionRequest?>?,
                                        token: PermissionToken?
                                ) { /* ... */
                                }
                            }).check()
                }
            }


        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                binding.spinKit.visibility = View.VISIBLE
                val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
                val resultUri: Uri = result!!.getUri()
                val file: File = File(resultUri.path)
                if (preference != null) {
                    AndroidNetworking.upload(APIURLs.BASE_URL + "user/change_picture")
                            .addMultipartFile("photo", file)
                            .addHeaders("accept", "application/json")
                            .addHeaders("Authorization", "Bearer " + preference.getString(Constants.LOGIN_TOKEN, "false"))
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
                                            if (response.getStatus()) {

                                                val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
                                                        ?: return

                                                with(preference.edit()) {
                                                    putString(Constants.PHOTO_URL, response.message)
                                                    apply()
                                                }

                                                Glide
                                                        .with(applicationContext)
                                                        .load(response.message)
                                                        .centerCrop()
                                                        .placeholder(R.mipmap.ic_launcher_round)
                                                        .into(binding.image)


                                                val mySnackbar = Snackbar.make(binding.lytParent, getString(R.string.success), Snackbar.LENGTH_SHORT)
                                                mySnackbar.show()

                                            } else {
                                                val mySnackbar = Snackbar.make(binding.lytParent, response.message, Snackbar.LENGTH_LONG)
                                                mySnackbar.show()
                                            }

                                        }

                                        override fun onError(anError: ANError) {
                                            binding.spinKit.visibility = View.GONE
                                            val mySnackbar = Snackbar.make(binding.lytParent, anError.errorDetail, Snackbar.LENGTH_SHORT)
                                            mySnackbar.show()
                                        }

                                    })
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error: Exception = result!!.getError()
            }
        }
    }

    private fun showAboutApp() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_app_about)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.findViewById<View>(R.id.bt_close).setOnClickListener { dialog.dismiss() }
        val preference = applicationContext?.getSharedPreferences(
                Constants.PREFERENCE_KEY,
                Context.MODE_PRIVATE
        )
        if (preference != null) {
            dialog.findViewById<TextView>(R.id.app_address).text = preference.getString(
                    Constants.TEAM_ADDRESS,
                    "Address"
            )
            dialog.findViewById<TextView>(R.id.app_text).text = preference.getString(
                    Constants.TEAM_NAME, resources.getString(
                    R.string.app_name
            )
            )
            dialog.findViewById<TextView>(R.id.app_phone).text = preference.getString(
                    Constants.TEAM_PHONE,
                    "Telephone"
            )
            dialog.findViewById<TextView>(R.id.app_email).text = preference.getString(
                    Constants.TEAM_EMAIL,
                    "Email"
            )
        }

        dialog.show()
    }

    private fun loadWebFromUrl() {
        val preference = applicationContext?.getSharedPreferences(
                Constants.PREFERENCE_KEY,
                Context.MODE_PRIVATE
        )
        if (preference != null) {
            val url = preference.getString(Constants.POLICY_URL, "null")
            if(url.equals("null"))
                return

            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

    }

}