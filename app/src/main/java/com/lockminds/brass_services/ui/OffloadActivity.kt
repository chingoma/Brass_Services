package com.lockminds.brass_services.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.lockminds.brass_services.*
import com.lockminds.brass_services.adapter.CheckPointSpinnerAdapter
import com.lockminds.brass_services.adapter.LoadStateAdapter
import com.lockminds.brass_services.adapter.ReasonsSpinnerAdapter
import com.lockminds.brass_services.adapter.ReceiverLotPagedAdapter
import com.lockminds.brass_services.databinding.ActivityOffloadBinding
import com.lockminds.brass_services.model.CheckPointActions
import com.lockminds.brass_services.model.Reasons
import com.lockminds.brass_services.model.ReceiverLot
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.viewmodel.*
import com.lockminds.libs.constants.APIURLs
import kotlinx.android.synthetic.main.custom_spinner_item.view.*
import kotlinx.android.synthetic.main.dialog_image.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.round


class OffloadActivity : BaseActivity() {

    private var actionJob: Boolean = false
    private var waitChanged: Boolean = false
    private var reasonId: String = ""
    lateinit var binding: ActivityOffloadBinding
    private lateinit var reasonsSpinnerAdapter: ReasonsSpinnerAdapter

    private lateinit var viewModel: ReceiverLotPagedViewModel

    private val reasonsViewModel by viewModels<ReasonsViewModel> {
        ReasonsViewModelFactory((application as App).reasons)
    }

    @ExperimentalPagingApi
    private val adapter = ReceiverLotPagedAdapter(this){ receiverLot -> receiverLotClickListener(
        receiverLot
        )}

    private var searchJob: Job? = null

    @ExperimentalPagingApi
    private fun search(query: String) {
        // Make sure we cancel the previous job before creating a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.getItems(applicationContext,query).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    @ExperimentalPagingApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOffloadBinding.inflate(layoutInflater)
        val view: View  = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this, Injection.receiverLotPagedViewModelFactory((this)))
            .get(ReceiverLotPagedViewModel::class.java)
        initComponent()
        initNavigationMenu()
        initAdapter()
        binding.toolbar.title = "Offload Lots"
        binding.toolbar.subtitle = "List of lots to offload"

    }

    override fun onResume() {
        super.onResume()
        syncReasons()
    }

    @ExperimentalPagingApi
    private fun receiverLotClickListener(lot: ReceiverLot) {

        if(actionJob){
            toast("Please wait...")
            return
        }

        showActionDialog(lot)

    }

    @ExperimentalPagingApi
    private fun initAdapter() {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            footer = LoadStateAdapter { adapter.retry() }
        )
        adapter.addLoadStateListener { loadState ->
            // Only show the list if refresh succeeds.
            binding.recyclerView.isVisible = true
            binding.loader.isVisible = false
            //loadState.source.refresh is LoadState.NotLoading
            // Show loading spinner during initial load or refresh.
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            // Show the retry state if initial load or refresh fails.
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {

            }
        }

        search(sessionManager.getWarehouse().toString())

        reasonsViewModel.getItems(sessionManager.getTeamId().toString()).observe(this){
            it?.let {
                reasonsSpinnerAdapter = ReasonsSpinnerAdapter(this,it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this@OffloadActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initComponent() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

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

    @ExperimentalPagingApi
    private fun showActionDialog(lot: ReceiverLot) {
        val materialDateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("SELECT RECEIVING DATE ")

        val materialDatePicker = materialDateBuilder.build()

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.offload_lot_dialog_layout)
        dialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.horizontalMargin = 15F

        val datePicker = dialog.findViewById<MaterialButton>(R.id.datePicker)
        val title = dialog.findViewById<TextView>(R.id.title)
        val heading = dialog.findViewById<TextView>(R.id.heading)
        val weight = dialog.findViewById<EditText>(R.id.weight)
        val weightContainer = dialog.findViewById<View>(R.id.weight_container)
        val reason = dialog.findViewById<Spinner>(R.id.reason)

        reason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {

                // It returns the clicked item.
                val clickedItem: Reasons =  parent.getItemAtPosition(position) as Reasons
                reasonId = clickedItem.id.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if(this::reasonsSpinnerAdapter.isInitialized){
            reason.adapter = reasonsSpinnerAdapter
        }

        weight.addTextChangedListener { wet ->
            weightContainer.isVisible = wet.toString() != "${lot.mine_gross_weight?.let { round(it.toDouble()) }}"
            waitChanged = wet.toString() != "${lot.mine_gross_weight?.let { round(it.toDouble()) }}"
        }

        heading.text = Tools.fromHtml("${lot.lot_no}")
        title.text = Tools.fromHtml("Weight = ${lot.mine_gross_weight?.let { round(it.toDouble()) }}")

        datePicker.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            datePicker.text = materialDatePicker.headerText
        }

        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

            if(actionJob){
                toast("please wait ...")
                return@setOnClickListener
            }

            if(weight.text.toString().isEmpty()){
                toast("please enter weight ...")
                return@setOnClickListener
            }

            if(datePicker.text.toString() == "Set Date"){
                toast("please pick date ...")
                return@setOnClickListener
            }

            if(reasonId.isEmpty() && waitChanged){
                toast("select reason ...")
                return@setOnClickListener
            }

            actionJob = true
            binding.loader.isVisible = true
            dialog.dismiss()
            AndroidNetworking.post(APIURLs.BASE_URL + "receiver/offload")
                .addBodyParameter("date", datePicker.text.toString())
                .addBodyParameter("lot", lot.lot_no)
                .addBodyParameter("reason", reasonId)
                .addBodyParameter("weight", weight.text.toString())
                .setPriority(Priority.HIGH)
                .build()
                .getAsObject(
                    Response::class.java,
                    object : ParsedRequestListener<Response> {
                        override fun onResponse(response: Response) {
                            binding.loader.isVisible = false
                            actionJob = false
                            search(sessionManager.getWarehouse().toString())
                            toast(response.message)
                        }

                        override fun onError(anError: ANError) {
                            actionJob = false
                            binding.loader.isVisible = false
                            toast(anError.message + anError.errorDetail)
                        }
                    })

        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun syncReasons(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_reasons")
            .setPriority(Priority.HIGH)
            .setPriority(Priority.LOW)
            .build()
            .getAsObjectList(
                Reasons::class.java,
                object : ParsedRequestListener<List<Reasons>> {
                    override fun onResponse(data: List<Reasons>) {
                        val itemsData: List<Reasons> = data
                        GlobalScope.launch {
                            (application as App).reasons.syncItems(sessionManager.getTeamId().toString(),itemsData)
                        }
                    }

                    override fun onError(anError: ANError) {}
                })
    }

}