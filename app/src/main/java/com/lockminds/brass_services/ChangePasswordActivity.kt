package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import com.lockminds.brass_services.databinding.ActivityChangePasswordBinding
import com.lockminds.brass_services.reponses.Response
import com.lockminds.libs.constants.APIURLs

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
        initComponents()
    }


    private fun initComponents() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)

        val preference = applicationContext?.getSharedPreferences(
                Constants.PREFERENCE_KEY,
                Context.MODE_PRIVATE
        )

        if (preference != null) {

            Glide
                    .with(applicationContext)
                    .load(preference.getString(Constants.PHOTO_URL, ""))
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(binding.image)
        }

        binding.submit.setOnClickListener {
            attemptUpdate()
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

    fun  disableFunctions(){
        binding.submit.isEnabled = false
        binding.submit.isClickable = false
    }

    fun enableFunction(){
        binding.submit.isEnabled = true
        binding.submit.isClickable = true
    }

    fun attemptUpdate(){

        var focusView: View? = null
        var cancel = false

        if (TextUtils.isEmpty(binding.newPassword.text.toString())) {
            binding.newPassword.error = getString(R.string.new_password)
            focusView = binding.newPassword
            cancel = true
        }

        if (TextUtils.isEmpty(binding.oldPassword.text.toString())) {
            binding.oldPassword.error = getString(R.string.old_password)
            focusView = binding.oldPassword
            cancel = true
        }

        if (!cancel) {
            binding.spinKit.visibility = View.VISIBLE
            disableFunctions()
            val android_id = Settings.Secure.getString(applicationContext?.getContentResolver(), Settings.Secure.ANDROID_ID)
            val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
            if (preference != null) {
                AndroidNetworking.post(APIURLs.BASE_URL + "user/change_password")
                        .addBodyParameter("old_password", binding.oldPassword.text.toString())
                        .addBodyParameter("new_password", binding.newPassword.text.toString())
                        .addBodyParameter("device_name",android_id)
                        .addHeaders("accept", "application/json")
                        .setPriority(Priority.HIGH)
                        .addHeaders("Authorization", "Bearer " + preference.getString(Constants.LOGIN_TOKEN, "false"))
                        .build()
                        .getAsParsed(
                                object : TypeToken<Response?>() {},
                                object : ParsedRequestListener<Response> {

                                    override fun onResponse(response: Response) {
                                        binding.spinKit.visibility = View.GONE
                                        if (response.getStatus()) {

                                            with(preference.edit()) {
                                                putString(Constants.LOGIN_TOKEN, response.message)
                                                apply()
                                            }

                                            val mySnackbar = Snackbar.make(binding.lytParent, getString(R.string.success), Snackbar.LENGTH_SHORT)
                                            mySnackbar.show()

                                            val intent = Intent(this@ChangePasswordActivity, ProfileActivity::class.java)
                                            startActivity(intent)
                                            finish()

                                        } else {
                                            val mySnackbar = Snackbar.make(binding.lytParent, response.message, Snackbar.LENGTH_LONG)
                                            mySnackbar.show()
                                            enableFunction()
                                        }

                                    }

                                    override fun onError(anError: ANError) {
                                        enableFunction()
                                        binding.spinKit.visibility = View.GONE
                                        val mySnackbar = Snackbar.make(binding.lytParent, anError.errorDetail, Snackbar.LENGTH_SHORT)
                                        mySnackbar.show()
                                    }

                                })
            }
        } else {
            enableFunction()
            focusView?.isFocused
            binding.spinKit.visibility = View.GONE
        }

    }

}