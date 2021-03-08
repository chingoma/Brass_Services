package com.lockminds.brass_services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import com.lockminds.brass_services.adapter.AdapterListAnimation
import com.lockminds.brass_services.adapter.AdapterListDestinations
import com.lockminds.brass_services.adapter.AdapterListLotDestinations
import com.lockminds.brass_services.databinding.ActivityLotDetailsBinding
import com.lockminds.brass_services.model.Destinations
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.reponses.LoginResponse
import com.lockminds.brass_services.reponses.LotResponse
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.utils.ItemAnimation
import com.lockminds.libs.constants.APIURLs
import com.lockminds.libs.constants.Constants
import java.util.*

class LotDetailsActivity : BaseActivity() {

    lateinit var binding: ActivityLotDetailsBinding

    private var parent_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterListLotDestinations? = null
    private val animation_type: Int = ItemAnimation.FADE_IN
    private var mBehavior: BottomSheetBehavior<*>? = null
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private val bottom_sheet: View? = null
    private var items: List<Destinations> = ArrayList()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLotDetailsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        binding.toolbar.navigationIcon = getDrawable(R.drawable.ic_back)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initController()
        initComponent()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
        binding.spinKit.isVisible = true
        fetchDestinations()
    }

    private fun initController(){
        val bundle: Bundle? = intent.extras
        binding.toolbar.title = bundle?.getString("lot_no")
        mBehavior = BottomSheetBehavior.from(binding.bottomSheet)
    }

    private fun initComponent() {
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        recyclerView!!.setHasFixedSize(true)

        binding.swiperefresh.setOnRefreshListener {
            fetchDestinations()
        }

        binding.checkPointIn.setOnClickListener {
            showBottomSheetDialog("DESTINATION IN")
        }
        binding.checkPointOut.setOnClickListener {
            showBottomSheetDialog("DESTINATION OUT")
        }
        binding.checkPointIn.isVisible = false
        binding.checkPointOut.isVisible = false
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this@LotDetailsActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }



    private fun setAdapter() {
        val bundle: Bundle? = intent.extras
        val lot_no = bundle?.getString("lot_no");
        val preference = applicationContext?.getSharedPreferences(
            Constants.PREFERENCE_KEY,
            Context.MODE_PRIVATE
        )
        if (preference != null) {
            AndroidNetworking.post(APIURLs.BASE_URL + "lots/check_point_history")
                .setTag("lots")
                .addBodyParameter("lot_no", lot_no)
                .addHeaders("accept", "application/json")
                .addHeaders(
                    "Authorization", "Bearer " + preference.getString(
                        Constants.LOGIN_TOKEN,
                        "false"
                    )
                )
                .setPriority(Priority.HIGH)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(
                    LotResponse::class.java,
                    object : ParsedRequestListener<List<LotResponse>> {
                        override fun onResponse(business: List<LotResponse>) {
                            val items: List<LotResponse> = business
                            Log.e("KELLY", items.toString())
                            if (items.size > 0) {
                                //set data and list adapter
                                mAdapter = AdapterListLotDestinations(
                                    applicationContext,
                                    items,
                                    animation_type
                                )
                                recyclerView!!.adapter = mAdapter
                                // on item list clicked

                            }
                        }

                        override fun onError(anError: ANError) {
                            Log.e("KELLY", anError.errorDetail)
                        }
                    })
        }
    }

    private fun fetchDestinations(){
        val preference = applicationContext?.getSharedPreferences(
            Constants.PREFERENCE_KEY,
            Context.MODE_PRIVATE
        )
        if (preference != null) {
            AndroidNetworking.get(APIURLs.BASE_URL + "get_destinations")
                .setTag("lots")
                .addHeaders("accept", "application/json")
                .addHeaders(
                    "Authorization", "Bearer " + preference.getString(
                        Constants.LOGIN_TOKEN,
                        "false"
                    )
                )
                .setPriority(Priority.HIGH)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(
                    Destinations::class.java,
                    object : ParsedRequestListener<List<Destinations>> {
                        override fun onResponse(data: List<Destinations>) {
                            binding.spinKit.isVisible = false
                            binding.swiperefresh.isRefreshing = false
                            val itemsData: List<Destinations> = data
                            if (itemsData.isNotEmpty()) {
                               items = itemsData
                                binding.checkPointIn.isVisible = true
                                binding.checkPointOut.isVisible = true
                            }
                        }

                        override fun onError(anError: ANError) {
                            binding.spinKit.isVisible = false
                            binding.swiperefresh.isRefreshing = false
                            Log.e("KELLY", anError.errorDetail)
                        }
                    })
        }

    }

    private fun showBottomSheetDialog(action: String) {
        if (mBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val view: View = layoutInflater.inflate(R.layout.add_destination_layout, null)
        var recyclerView: RecyclerView? = null
        var mAdapter: AdapterListDestinations? = null
        val animation_type: Int = ItemAnimation.FADE_IN
        val action_title : TextView = view.findViewById<View>(R.id.action_title) as TextView

        if(action == "DESTINATION IN"){
            action_title.text = "CHECK IN ACTION"
        }else{
            action_title.text = "CHECK out ACTION"
        }
        recyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        recyclerView!!.setHasFixedSize(true)

        mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog!!.setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        mBottomSheetDialog!!.show()
        mBottomSheetDialog!!.setOnDismissListener { mBottomSheetDialog = null }

        //set data and list adapter
        mAdapter = AdapterListDestinations(applicationContext, items, animation_type)
        recyclerView!!.adapter = mAdapter
        // on item list clicked
        mAdapter!!.setOnItemClickListener { view, obj, position ->
            mBottomSheetDialog!!.dismiss()

            binding.spinKit.visibility = View.VISIBLE
            disableFunctions()
            val bundle: Bundle? = intent.extras
            val lot_no = bundle?.getString("lot_no")
            val preference = applicationContext?.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)
            if (preference != null) {
                AndroidNetworking.post(APIURLs.BASE_URL + "lots/add_destination")
                    .setTag("lots")
                    .addBodyParameter("destination",obj.id)
                    .addBodyParameter("action",action)
                    .addBodyParameter("lot_no",lot_no)
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " +preference.getString(Constants.LOGIN_TOKEN, "false"))
                    .setPriority(Priority.HIGH)
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsParsed(
                        object : TypeToken<Response?>() {},
                        object : ParsedRequestListener<Response> {

                            override fun onResponse(response: Response) {
                                binding.spinKit.visibility = View.GONE
                                if (response.getStatus()) {
                                    setAdapter()
                                }
                                val mySnackbar = Snackbar.make(binding.appbar, response.message, Snackbar.LENGTH_SHORT)
                                mySnackbar.show()
                                enableFunction()
                            }

                            override fun onError(anError: ANError) {
                                enableFunction()
                                binding.spinKit.visibility = View.GONE
                                val mySnackbar = Snackbar.make(binding.appbar, anError.errorDetail, Snackbar.LENGTH_SHORT)
                                mySnackbar.show()
                            }

                        })
            }

        }

    }

    fun  disableFunctions(){
        binding.checkPointOut.isEnabled = false
        binding.checkPointOut.isClickable = false
        binding.checkPointIn.isEnabled = false
        binding.checkPointIn.isClickable = false

    }

    fun enableFunction(){
        binding.checkPointOut.isEnabled = true
        binding.checkPointOut.isClickable = true
        binding.checkPointIn.isEnabled = true
        binding.checkPointIn.isClickable = true
    }

}