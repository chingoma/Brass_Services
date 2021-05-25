package com.lockminds.brass_services.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.SpinKitView
import com.lockminds.brass_services.*
import com.lockminds.brass_services.adapter.AccidentAdapter
import com.lockminds.brass_services.adapter.LotsSpinnerAdapter
import com.lockminds.brass_services.databinding.ActivityAccidentsBinding
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.viewmodel.*
import com.lockminds.libs.constants.APIURLs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AccidentsActivity : BaseActivity() {

    lateinit var binding: ActivityAccidentsBinding

    private val accidentsViewModel by viewModels<AccidentViewModel> {
        AccidentViewModelFactory((application as App).accidents)
    }

    private val lotSpinnerViewModel by viewModels<LotSpinnerViewModel>{
        LotSpinnerViewModelFactory((application as App).lotRepo)
    }

    private lateinit var lotSpinnerAdapter: LotsSpinnerAdapter

    private var actionJob: Boolean = false
    private var checkPointId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccidentsBinding.inflate(layoutInflater)
        val view: View  = binding.root
        setContentView(view)
        initComponent()
        binding.lytNoConnection.isVisible = true
        setAdapter()
        initNavigationMenu()
    }

    override fun onResume() {
        super.onResume()
        syncAccidents()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this@AccidentsActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initComponent() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        binding.toolbar.title = "Accidents"
        binding.toolbar.subtitle = "Accidents History"

        binding.reportAccident.setOnClickListener {
            showActionDialog()
        }
    }

    private fun initNavigationMenu() {
        val drawer = binding.drawerLayout
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
        }

        drawer.addDrawerListener(toggle)

        // open/close drawer at start
        binding.navIcon.setOnClickListener{
            drawer.openDrawer(GravityCompat.START)
        }
    }


    private fun setAdapter() {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        val lotsAdapter = AccidentAdapter(this) { accident -> lotAdapterOnClickNear(
                accident
        ) }

        accidentsViewModel.allItems(sessionManager.getUserId().toString()).observe(this){

            it?.let {
                lotsAdapter.submitList(it)
                if(it.isNotEmpty()){
                    binding.lytNoConnection.isVisible = false
                }

                binding.lotQty.text = "Accidents ("+it.size.toString()+")"
            }
        }

        binding.recyclerView.adapter = lotsAdapter

        lotSpinnerViewModel.getItems().observe(this){
            it?.let {
                lotSpinnerAdapter = LotsSpinnerAdapter(this,it)
            }
        }

    }

    private fun syncAccidents(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_accidents")
            .setTag("lots")
            .addHeaders("accept", "application/json")
            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
            .setPriority(Priority.HIGH)
            .setPriority(Priority.LOW)
            .build()
            .getAsObjectList(Accident::class.java, object : ParsedRequestListener<List<Accident>> {
                override fun onResponse(list: List<Accident>) {
                    val items: List<Accident> = list
                    GlobalScope.launch {
                        (application as App).accidents.syncItems(
                                sessionManager.getUserId().toString(), items
                        )
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@AccidentsActivity, anError.errorDetail, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun showActionDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.add_accident_layout)
        dialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.horizontalMargin = 15F
        val checkPoint = dialog.findViewById<Spinner>(R.id.check_point)

        checkPoint.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
            ) {

                val clickedItem: Lot =  parent.getItemAtPosition(position) as Lot
                checkPointId = clickedItem.lot_no.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if(this::lotSpinnerAdapter.isInitialized){
            checkPoint.adapter = lotSpinnerAdapter
        }

        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        val today = Calendar.getInstance()
        val month = today.get(Calendar.MONTH) + 1
        var date = today.get(Calendar.DAY_OF_MONTH).toString()+"-"+month+"-"+today.get(Calendar.YEAR)
        datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)

        ) { view, year, month, day ->
            val month = month + 1
             date = "$year-$month-$day"
        }

        val description = dialog.findViewById<EditText>(R.id.description)
        val location = dialog.findViewById<EditText>(R.id.location)
        val spinKitView = dialog.findViewById<SpinKitView>(R.id.spin_kit)
        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

            if(actionJob){
                Toast.makeText(this@AccidentsActivity, "Please wait ...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPointId.isEmpty()){
                Toast.makeText(this@AccidentsActivity, "Select Lot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(description.text.isEmpty()){
                Toast.makeText(this@AccidentsActivity, "Please enter description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(location.text.isEmpty()){
                Toast.makeText(this@AccidentsActivity, "Please enter location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actionJob = true
            spinKitView.isVisible = true
            AndroidNetworking.post(APIURLs.BASE_URL + "reports/report_accident")
                    .addBodyParameter("lot", checkPointId)
                    .addBodyParameter("date", date)
                    .addBodyParameter("description", description.text.toString())
                    .addBodyParameter("location", location.text.toString())
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsObject(
                            Response::class.java,
                            object : ParsedRequestListener<Response> {
                                override fun onResponse(response: Response) {
                                    dialog.dismiss()
                                    actionJob = false
                                    spinKitView.isVisible = false
                                    syncAccidents()
                                    Toast.makeText(this@AccidentsActivity, response.message, Toast.LENGTH_SHORT).show()
                                }

                                override fun onError(anError: ANError) {
                                    actionJob = false
                                    spinKitView.isVisible = false
                                    Toast.makeText(this@AccidentsActivity, anError.message + anError.errorDetail, Toast.LENGTH_SHORT).show()
                                }
                            })

        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun lotAdapterOnClickNear(accident: Accident) {
        val lot = accident.id
        startActivity(AccidentActivity.createIntentAccident(this, lot.toString()))
    }

}