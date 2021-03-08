package com.lockminds.brass_services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.lockminds.brass_services.adapter.AdapterListAnimation
import com.lockminds.brass_services.databinding.ActivityMainBinding
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.utils.ItemAnimation
import com.lockminds.libs.constants.APIURLs
import com.lockminds.libs.constants.Constants

class MainActivity : BaseActivity() {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterListAnimation? = null
    private val animation_type: Int = ItemAnimation.FADE_IN

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View  = binding.root
        setContentView(view)
        initComponent()
        binding.spinKit.isVisible = true
        setAdapter()
    }


    override fun onResume() {
        super.onResume()
        val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (preference != null) {
            binding.toolbar.title = preference.getString(Constants.NAME, resources.getString(R.string.app_name))
            binding.toolbar.subtitle = preference.getString(Constants.EMAIL, "")

            Glide
                .with(applicationContext)
                .load(preference.getString(Constants.PHOTO_URL, ""))
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.image)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initComponent() {
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        recyclerView!!.setHasFixedSize(true)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)

        binding.swiperefresh.setOnRefreshListener {
            setAdapter()
        }

        val preference = applicationContext?.getSharedPreferences(
                Constants.PREFERENCE_KEY,
                Context.MODE_PRIVATE
        )



        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // filter recycler view when query submitted
                mAdapter?.filter?.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // filter recycler view when text is changed
                mAdapter?.filter?.filter(query)
                return false
            }
        })

    }

    private fun setAdapter() {
        val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (preference != null) {
            AndroidNetworking.get(APIURLs.BASE_URL + "lots/get_all_lots")
                    .setTag("lots")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + preference.getString(Constants.LOGIN_TOKEN, "false"))
                    .setPriority(Priority.HIGH)
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsObjectList(Lot::class.java, object : ParsedRequestListener<List<Lot>> {
                        override fun onResponse(business: List<Lot>) {
                            binding.spinKit.isVisible = false
                            binding.swiperefresh.isRefreshing = false
                            val items: List<Lot> = business
                            Log.e("KELLY", items.toString())
                            if (items.size > 0) {
                                //set data and list adapter
                                mAdapter = AdapterListAnimation(applicationContext, items, animation_type)
                                recyclerView!!.adapter = mAdapter
                                // on item list clicked
                                mAdapter!!.setOnItemClickListener { view, obj, position ->
                                    val intent = Intent(applicationContext, LotDetailsActivity::class.java)
                                    intent.putExtra("lot_no", obj.lot_no)
                                    startActivity(intent)
                                }
                            }
                        }

                        override fun onError(anError: ANError) {
                            binding.spinKit.isVisible = false
                            Log.e("KELLY", anError.errorDetail)
                        }
                    })
        }


    }


}