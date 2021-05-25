package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.lockminds.brass_services.Constants.Companion.LOGIN_STATUS
import com.lockminds.brass_services.Constants.Companion.PREFERENCE_KEY
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.model.Permissions
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

    lateinit var sessionManager: SessionManager
    lateinit var userViewModel: UserViewModel
    lateinit var permissionsViewModel: PermissionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setContentView(layoutResID)
        Tools.setSystemBarLight(this)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
    }

    override fun onResume() {
        super.onResume()
        checkLogin()
        initNavigation()
        syncPermissions()
        checkPermissions()
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

    fun syncPermissions(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_permissions")
            .setTag("permissions")
            .addHeaders("accept", "application/json")
            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
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
                val intent = Intent(applicationContext,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
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
            val intent = Intent(this@BaseActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }



    }

    override fun onUserInteraction() {
        super.onUserInteraction()

    }

    private var _idleHandler: Handler = Handler()
    private var _idleRunnable = Runnable {
        //handle your IDLE state
    }

    open fun delayedIdle(delayMinutes: Long) {
        _idleHandler.removeCallbacks(_idleRunnable)
        _idleHandler.postDelayed(_idleRunnable, (delayMinutes * 1000 * 60))
    }

}

