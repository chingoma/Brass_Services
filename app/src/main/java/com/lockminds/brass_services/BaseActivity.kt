package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.lockminds.libs.constants.Constants.Companion.LOGIN_STATUS
import com.lockminds.libs.constants.Constants.Companion.PREFERENCE_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

open class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    protected var MyIp: String? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Tools.setSystemBarLight(this)
        MyIp = Tools.getLocalIpAddress()
        Tools.NetPolicy()
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
    }

    override fun onResume() {
        super.onResume()
        val preference = applicationContext?.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (preference != null) {
            if(preference.getString(LOGIN_STATUS,"false")  == "false"){
                val intent = Intent(applicationContext,LoginActivity::class.java)
                startActivity(intent)
                finish()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
//            R.id.settings -> {
//                val intent = Intent(this, SettingsActivity::class.java).apply { }
//                startActivity(intent)
//                true
//            }
//
//            R.id.edit_profile -> {
//                val intent = Intent(this, ProfileEditActivity::class.java).apply { }
//                startActivity(intent)
//                true
//            }
//
//            R.id.logout -> {
//                Firebase.auth.signOut()
//                val intent = Intent(this, AuthUiActivity::class.java).apply { }
//                startActivity(intent)
//                true
//            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    open fun toastIconError(message: String?) {
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_LONG

        //inflate view
        val custom_view: View = layoutInflater.inflate(R.layout.toast_icon_text, null)
        (custom_view.findViewById<View>(R.id.message) as TextView).text = message
        (custom_view.findViewById<View>(R.id.icon) as ImageView).setImageResource(R.drawable.ic_close)
        (custom_view.findViewById<View>(R.id.parent_view) as CardView).setCardBackgroundColor(
                resources.getColor(R.color.red_600)
        )
        toast.setView(custom_view)
        toast.show()
    }

    open fun toastIconSuccess(message: String?) {
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_LONG

        //inflate view
        val custom_view: View = layoutInflater.inflate(R.layout.toast_icon_text, null)
        (custom_view.findViewById<View>(R.id.message) as TextView).text = message
        (custom_view.findViewById<View>(R.id.icon) as ImageView).setImageResource(R.drawable.ic_done)
        (custom_view.findViewById<View>(R.id.parent_view) as CardView).setCardBackgroundColor(
                resources.getColor(R.color.green_500)
        )
        toast.setView(custom_view)
        toast.show()
    }

    open fun toastIconInfo(message: String?) {
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_LONG

        //inflate view
        val custom_view: View = layoutInflater.inflate(R.layout.toast_icon_text, null)
        (custom_view.findViewById<View>(R.id.message) as TextView).text = message
        (custom_view.findViewById<View>(R.id.icon) as ImageView).setImageResource(R.drawable.ic_error_outline)
        (custom_view.findViewById<View>(R.id.parent_view) as CardView).setCardBackgroundColor(
                resources.getColor(R.color.blue_500)
        )
        toast.setView(custom_view)
        toast.show()
    }

    private var progressBar: ProgressBar? = null

    fun setProgressBar(bar: ProgressBar) {
        progressBar = bar
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    public override fun onStop() {
        super.onStop()
        hideProgressBar()
    }



}

