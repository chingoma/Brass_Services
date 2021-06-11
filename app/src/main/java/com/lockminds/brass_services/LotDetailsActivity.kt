package com.lockminds.brass_services

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.github.ybq.android.spinkit.SpinKitView
import com.lockminds.brass_services.adapter.ActionsSpinnerAdapter
import com.lockminds.brass_services.adapter.CheckPointSpinnerAdapter
import com.lockminds.brass_services.adapter.LotHistoryAdapter
import com.lockminds.brass_services.adapter.LotsSpinnerAdapter
import com.lockminds.brass_services.databinding.ActivityLotDetailsBinding
import com.lockminds.brass_services.model.*
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.ui.AccidentsActivity
import com.lockminds.brass_services.viewmodel.*
import com.lockminds.libs.constants.APIURLs
import com.lockminds.brass_services.Constants.Companion.INTENT_PARAM_1
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class LotDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityLotDetailsBinding
    private lateinit var lot: Lot
    private lateinit var actionsSpinnerAdapter: ActionsSpinnerAdapter
    private lateinit var checkPointSpinnerAdapter: CheckPointSpinnerAdapter
    private var actionJob: Boolean = false
    private var checkPointId: String = ""
    private var checkPointActionId: String = ""
    private lateinit var lotSpinnerAdapter: LotsSpinnerAdapter

    private val actionsViewModel by viewModels<ActionsViewModel> {
        ActionsViewModelFactory((application as App).checkPointsAction)
    }

    private val checkPointViewModel by viewModels<CheckPointViewModel>{
        CheckPointViewModelFactory((application as App).checkPoints)
    }

    private val lotHistoryViewModel by viewModels<LotHistoryViewModel>{
        LotHistoryViewModelFactory((application as App).checkPointHistory)
    }

    private val lotSpinnerViewModel by viewModels<LotSpinnerViewModel>{
        LotSpinnerViewModelFactory((application as App).lotRepo)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLotDetailsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lot = intent.getParcelableExtra(INTENT_PARAM_1)!!
        binding.toolbar.navigationIcon = getDrawable(R.drawable.ic_back)
        setSupportActionBar(binding.toolbar)

        initComponent()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
        syncDatabase()
    }

    private fun initComponent() {
        supportActionBar!!.title = lot.lot_no
        supportActionBar!!.subtitle = lot.source+" - "+lot.driver
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        binding.reportAction.setOnClickListener {
            showActionDialog()
        }

        binding.reportAccident.setOnClickListener {
            reportAccident()
        }

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

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        val lotHistoryAdapter = LotHistoryAdapter(this){
            lot -> lotAdapterOnClickNear(lot)
        }

        actionsViewModel.getItems().observe(this){
            it?.let {
                actionsSpinnerAdapter = ActionsSpinnerAdapter(this,it)
            }
        }

        checkPointViewModel.getItems().observe(this){
            it?.let {
                checkPointSpinnerAdapter = CheckPointSpinnerAdapter(this,it)
            }
        }

        lotHistoryViewModel.getItems(lot.lot_no.toString()).observe(this){
            it?.let{
                lotHistoryAdapter.submitList(it)
                if(it.isNotEmpty()){
                    binding.lytNoConnection.isVisible = false
                }
            }
        }

        binding.recyclerView.adapter = lotHistoryAdapter

        lotSpinnerViewModel.getItems().observe(this){
            it?.let {
                lotSpinnerAdapter = LotsSpinnerAdapter(this,it)
            }
        }

    }


    private fun syncDatabase(){
        syncCheckpoints()
        syncCheckpointActions()
        syncCheckpointHistory()
    }

    private fun syncCheckpoints(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_check_points")
                .setPriority(Priority.HIGH)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(
                        CheckPoint::class.java,
                        object : ParsedRequestListener<List<CheckPoint>> {
                            override fun onResponse(data: List<CheckPoint>) {
                                val itemsData: List<CheckPoint> = data
                                GlobalScope.launch {
                                    (application as App).checkPoints.syncItems(itemsData)
                                }
                            }

                            override fun onError(anError: ANError) {}
                        })
    }

    private fun syncCheckpointHistory(){
        AndroidNetworking.get(APIURLs.BASE_URL + "lots/check_point_history")
                .setTag("lots")
                .addQueryParameter("lot_no",lot.lot_no)
                .setPriority(Priority.HIGH)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(
                        CheckPointHistory::class.java,
                        object : ParsedRequestListener<List<CheckPointHistory>> {
                            override fun onResponse(data: List<CheckPointHistory>) {
                                val itemsData: List<CheckPointHistory> = data
                                GlobalScope.launch {
                                    (application as App).checkPointHistory.syncItems(itemsData)
                                }
                            }

                            override fun onError(anError: ANError) {}
                        })
    }

    private fun syncCheckpointActions(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_check_point_actions")
                .setPriority(Priority.HIGH)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(
                        CheckPointActions::class.java,
                        object : ParsedRequestListener<List<CheckPointActions>> {
                            override fun onResponse(data: List<CheckPointActions>) {
                                val itemsData: List<CheckPointActions> = data
                                GlobalScope.launch {
                                    (application as App).checkPointsAction.syncItems(itemsData)
                                }
                            }

                            override fun onError(anError: ANError) {}
                        })
    }


    private fun showActionDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.add_action_layout)
        dialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val description = dialog.findViewById<EditText>(R.id.description)
        val actions = dialog.findViewById<Spinner>(R.id.action)
        val checkPoint = dialog.findViewById<Spinner>(R.id.check_point)
        dialog.findViewById<TextView>(R.id.lot_no_action).text = lot.lot_no
        dialog.findViewById<TextView>(R.id.lot_source_action).text = lot.source+" - "+lot.driver


        actions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
            ) {

                // It returns the clicked item.
                val clickedItem: CheckPointActions =  parent.getItemAtPosition(position) as CheckPointActions
                checkPointActionId = clickedItem.id.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        checkPoint.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
            ) {

                // It returns the clicked item.
                val clickedItem: CheckPoint =  parent.getItemAtPosition(position) as CheckPoint
                checkPointId = clickedItem.id.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if(this::actionsSpinnerAdapter.isInitialized){
            actions.adapter = actionsSpinnerAdapter
        }

        if(this::checkPointSpinnerAdapter.isInitialized){
            checkPoint.adapter = checkPointSpinnerAdapter
        }

        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

            if(actionJob){
                Toast.makeText(this@LotDetailsActivity, "Please wait ...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPointId.isEmpty()){
                Toast.makeText(this@LotDetailsActivity, "Select check point", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPointActionId.isEmpty()){
                Toast.makeText(this@LotDetailsActivity, "Select action", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            Toast.makeText(this@LotDetailsActivity, "Sending action", Toast.LENGTH_SHORT).show()
            actionJob = true
            AndroidNetworking.post(APIURLs.BASE_URL + "reports/report_action")
                    .addBodyParameter("id", lot.lot_no)
                    .addBodyParameter("check_point", checkPointId)
                    .addBodyParameter("check_point_action", checkPointActionId)
                    .addBodyParameter("description", description.text.toString())
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsObject(
                            Response::class.java,
                            object : ParsedRequestListener<Response> {
                                override fun onResponse(response: Response) {
                                    dialog.dismiss()
                                    actionJob = false
                                    syncCheckpointHistory()
                                    Toast.makeText(this@LotDetailsActivity, response.message, Toast.LENGTH_SHORT).show()
                                }

                                override fun onError(anError: ANError) {
                                    actionJob = false
                                    Toast.makeText(this@LotDetailsActivity, anError.message + anError.errorDetail, Toast.LENGTH_SHORT).show()
                                }
                            })

        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun reportAccident() {

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
                Toast.makeText(this@LotDetailsActivity, "Please wait ...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPointId.isEmpty()){
                Toast.makeText(this@LotDetailsActivity, "Select Lot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(description.text.isEmpty()){
                Toast.makeText(this@LotDetailsActivity, "Please enter description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(location.text.isEmpty()){
                Toast.makeText(this@LotDetailsActivity, "Please enter location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actionJob = true
            spinKitView.isVisible = true
            AndroidNetworking.post(APIURLs.BASE_URL + "reports/report_accident")
                    .addBodyParameter("lot", checkPointId)
                    .addBodyParameter("date", date)
                    .addBodyParameter("description", description.text.toString())
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsObject(
                            Response::class.java,
                            object : ParsedRequestListener<Response> {
                                override fun onResponse(response: Response) {
                                    dialog.dismiss()
                                    actionJob = false
                                    spinKitView.isVisible = false
                                    Toast.makeText(this@LotDetailsActivity, response.message, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@LotDetailsActivity, AccidentsActivity::class.java)
                                    startActivity(intent)
                                }

                                override fun onError(anError: ANError) {
                                    actionJob = false
                                    spinKitView.isVisible = false
                                    Toast.makeText(this@LotDetailsActivity, anError.message + anError.errorDetail, Toast.LENGTH_SHORT).show()
                                }
                            })

        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun lotAdapterOnClickNear(data: CheckPointHistory) {
        showLotMore(data)
    }

    private fun showLotMore(data: CheckPointHistory) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.lot_history_more)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.findViewById<View>(R.id.bt_close).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<TextView>(R.id.lot_no).text = data.lot_no
        dialog.findViewById<TextView>(R.id.description).text = data.action_description
        dialog.findViewById<TextView>(R.id.action).text = data.action
        dialog.findViewById<TextView>(R.id.date).text = data.created_at
        dialog.show()
    }


    companion object {
        @JvmStatic
        fun createIntent(context: Context, lot: Lot?): Intent {
            return Intent().setClass(context, LotDetailsActivity::class.java)
                .putExtra(INTENT_PARAM_1, lot)
        }
    }

}