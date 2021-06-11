package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.lockminds.brass_services.databinding.ActivityUpdateProfileBinding
import com.lockminds.brass_services.reponses.Response
import com.lockminds.libs.constants.APIURLs

class UpdateProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityUpdateProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        initControllers()
        initComponents()
    }

    fun initControllers(){
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
    }

    private fun initComponents() {

        val preference = applicationContext?.getSharedPreferences(
            Constants.PREFERENCE_KEY,
            Context.MODE_PRIVATE
        )

        if (preference != null) {
            binding.name.setText(preference.getString(Constants.NAME,""))
            binding.phoneNumber.setText(preference.getString(Constants.PHONE_NUMBER,""))

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
        binding.name.isClickable = false
        binding.name.isEnabled = false
        binding.phoneNumber.isClickable = false
        binding.phoneNumber.isEnabled = false
    }

    fun enableFunction(){
        binding.submit.isEnabled = true
        binding.submit.isClickable = true
        binding.name.isClickable = true
        binding.name.isEnabled = true
        binding.phoneNumber.isClickable = true
        binding.phoneNumber.isEnabled = true
    }

    fun attemptUpdate(){

        var focusView: View? = null
        var cancel = false

        if (TextUtils.isEmpty(binding.name.text.toString())) {
            binding.name.error = getString(R.string.name)
            focusView = binding.name
            cancel = true
        }

        if (TextUtils.isEmpty(binding.phoneNumber.text.toString())) {
            binding.phoneNumber.error = getString(R.string.enter_phone_number)
            focusView = binding.phoneNumber
            cancel = true
        }

        if (!cancel) {
            binding.spinKit.visibility = View.VISIBLE
            disableFunctions()
            val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
            if (preference != null) {
                AndroidNetworking.post(APIURLs.BASE_URL + "user/change_details")
                    .addBodyParameter("name", binding.name.text.toString())
                    .addBodyParameter("phone_number", binding.phoneNumber.text.toString())
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsParsed(
                        object : TypeToken<Response?>() {},
                        object : ParsedRequestListener<Response> {

                            override fun onResponse(response: Response) {
                                binding.spinKit.visibility = View.GONE
                                if (response.getStatus()) {

                                    val preference = applicationContext?.getSharedPreferences(
                                        Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
                                        ?: return

                                    with(preference.edit()) {
                                        putString(Constants.NAME, binding.name.text.toString())
                                        putString(Constants.PHONE_NUMBER, binding.phoneNumber.text.toString())
                                        apply()
                                    }

                                    val mySnackbar = Snackbar.make(binding.lytParent, getString(R.string.success), Snackbar.LENGTH_SHORT)
                                    mySnackbar.show()

                                    val intent = Intent(this@UpdateProfileActivity, ProfileActivity::class.java)
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