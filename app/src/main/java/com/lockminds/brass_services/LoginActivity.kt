package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import com.lockminds.brass_services.databinding.ActivityLoginBinding
import com.lockminds.brass_services.reponses.LoginResponse
import com.lockminds.libs.constants.APIURLs
import com.lockminds.brass_services.Constants.Companion.LOGIN_STATUS
import com.lockminds.brass_services.Constants.Companion.LOGIN_TOKEN
import com.lockminds.brass_services.Constants.Companion.PREFERENCE_KEY
import com.lockminds.brass_services.Constants.Companion.USER_ID
import android.provider.Settings.Secure
import com.lockminds.brass_services.Constants.Companion.CHANGE_DETAILS
import com.lockminds.brass_services.Constants.Companion.CHANGE_PICTURE
import com.lockminds.brass_services.Constants.Companion.EMAIL
import com.lockminds.brass_services.Constants.Companion.NAME
import com.lockminds.brass_services.Constants.Companion.PHONE_NUMBER
import com.lockminds.brass_services.Constants.Companion.PHOTO_URL
import com.lockminds.brass_services.Constants.Companion.POLICY_URL
import com.lockminds.brass_services.Constants.Companion.TEAM_ADDRESS
import com.lockminds.brass_services.Constants.Companion.TEAM_EMAIL
import com.lockminds.brass_services.Constants.Companion.TEAM_NAME
import com.lockminds.brass_services.Constants.Companion.TEAM_PHONE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
        binding.loginBtn.setOnClickListener {
            attemptLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        val preference = applicationContext?.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (preference != null) {
            if(preference.getString(LOGIN_STATUS,"false")  == "true"){
                val intent = Intent(applicationContext,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    fun  disableFunctions(){
        binding.loginBtn.isEnabled = false
        binding.loginBtn.isClickable = false
        binding.username.isClickable = false
        binding.username.isEnabled = false
        binding.password.isClickable = false
        binding.password.isEnabled = false

    }

    fun enableFunction(){
        binding.loginBtn.isEnabled = true
        binding.loginBtn.isClickable = true
        binding.username.isClickable = true
        binding.username.isEnabled = true
        binding.password.isClickable = true
        binding.password.isEnabled = true
    }

    fun attemptLogin(){

        var focusView: View? = null
        var cancel = false

        if (TextUtils.isEmpty(binding.username.text.toString())) {
            binding.username.error = getString(R.string.enter_username)
            focusView = binding.username
            cancel = true
        }

        if (TextUtils.isEmpty(binding.password.text.toString())) {
            binding.password.error = getString(R.string.enter_password)
            focusView = binding.password
            cancel = true
        }

        if (!cancel) {
            binding.spinKit.visibility = View.VISIBLE
            disableFunctions()
            val android_id = Secure.getString(applicationContext?.getContentResolver(), Secure.ANDROID_ID)
            AndroidNetworking.post(APIURLs.BASE_URL + "login/token")
                .addBodyParameter("username", binding.username.text.toString())
                .addBodyParameter("password", binding.password.text.toString())
                .addBodyParameter("device_name",android_id)
                .setTag("token_request")
                .addHeaders("accept", "application/json")
                .setPriority(Priority.HIGH)
                .build()
                .getAsParsed(
                        object : TypeToken<LoginResponse?>() {},
                        object : ParsedRequestListener<LoginResponse> {

                            override fun onResponse(response: LoginResponse) {
                                binding.spinKit.visibility = View.GONE

                                if (response.status) {

                                GlobalScope.launch {
                                    (application as App).user.syncUser(response.user.user_id.toString(),response.user)
                                }


                                    val preference = applicationContext?.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                                            ?: return

                                    with(preference.edit()) {
                                        putString(LOGIN_STATUS, "true")
                                        putString(LOGIN_TOKEN, response.token)
                                        putString(NAME, response.name)
                                        putString(PHONE_NUMBER, response.phone_number)
                                        putString(PHOTO_URL, response.photo_url)
                                        putString(USER_ID, response.id)
                                        putString(EMAIL, response.email)
                                        putString(POLICY_URL, response.policy_url)
                                        putString(TEAM_EMAIL, response.team_email)
                                        putString(TEAM_NAME, response.team_name)
                                        putString(TEAM_PHONE, response.team_phone)
                                        putString(TEAM_ADDRESS, response.team_address)
                                        putString(CHANGE_DETAILS, response.change_details)
                                        putString(CHANGE_PICTURE, response.change_picture)
                                        apply()
                                    }

                                    val mySnackbar = Snackbar.make(binding.lytParent, getString(R.string.success), Snackbar.LENGTH_SHORT)
                                    mySnackbar.show()

                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)

                                } else {
                                    val mySnackbar = Snackbar.make(binding.lytParent, response.message, Snackbar.LENGTH_LONG)
                                    mySnackbar.show()
                                    enableFunction()
                                }

                            }

                            override fun onError(anError: ANError) {
                                enableFunction()
                                Log.e("KELLY", anError.toString())
                                binding.spinKit.visibility = View.GONE
                                val mySnackbar = Snackbar.make(binding.lytParent, anError.errorDetail, Snackbar.LENGTH_SHORT)
                                mySnackbar.show()
                                Log.e("KELLY", anError.errorBody)
                            }

                        })
        } else {
            enableFunction()
            focusView?.isFocused
            binding.spinKit.visibility = View.GONE
        }

    }

}