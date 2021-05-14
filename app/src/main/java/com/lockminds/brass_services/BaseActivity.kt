package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.lockminds.brass_services.Constants.Companion.LOGIN_STATUS
import com.lockminds.brass_services.Constants.Companion.PREFERENCE_KEY
import com.lockminds.brass_services.ui.AttendanceActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

open class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)
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

        val sessionManager = SessionManager(applicationContext)
        findViewById<TextView>(R.id.drawer_name)?.text = sessionManager.getName()
        findViewById<TextView>(R.id.drawer_email)?.text = sessionManager.getEmail()
        val drawerImage = findViewById<ImageView>(R.id.drawer_image)

        if(drawerImage != null){
            Glide
                .with(applicationContext)
                .load(sessionManager.getPhotoUrl())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .into(drawerImage)
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

