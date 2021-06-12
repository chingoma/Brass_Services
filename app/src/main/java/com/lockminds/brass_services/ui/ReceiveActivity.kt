package com.lockminds.brass_services.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.lockminds.brass_services.*
import com.lockminds.brass_services.adapter.LoadStateAdapter
import com.lockminds.brass_services.adapter.ReceiverLotPagedAdapter
import com.lockminds.brass_services.databinding.ActivityReceiveBinding
import com.lockminds.brass_services.model.ReceiverLot
import com.lockminds.brass_services.reponses.Response
import com.lockminds.brass_services.viewmodel.ReceiverLotPagedViewModel
import com.lockminds.libs.constants.APIURLs
import kotlinx.android.synthetic.main.custom_spinner_item.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*


class ReceiveActivity : BaseActivity() {

    private var actionJob: Boolean = false

    lateinit var binding: ActivityReceiveBinding

    private lateinit var viewModel: ReceiverLotPagedViewModel

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
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        val view: View  = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this, Injection.receiverLotPagedViewModelFactory((this)))
            .get(ReceiverLotPagedViewModel::class.java)
        initComponent()
        initNavigationMenu()
        initAdapter()
        binding.toolbar.title = "Receive Lots"
        binding.toolbar.subtitle = "List of lots to receive"

    }

    override fun onResume() {
        super.onResume()
    }

    @ExperimentalPagingApi
    private fun receiverLotClickListener(lot: ReceiverLot) {

        if(actionJob){
            toast("Please wait...")
            return
        }

        val materialDateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("SELECT RECEIVING DATE ")

        val materialDatePicker = materialDateBuilder.build()
        materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")

        materialDatePicker.addOnPositiveButtonClickListener {
            actionJob = true
            binding.loader.isVisible = true
            AndroidNetworking.post(APIURLs.BASE_URL + "receiver/receive")
                .addBodyParameter("lot", lot.lot_no)
                .addBodyParameter("date", materialDatePicker.headerText)
                .setPriority(Priority.HIGH)
                .build()
                .getAsObject(
                    Response::class.java,
                    object : ParsedRequestListener<Response> {
                        override fun onResponse(response: Response) {
                            binding.loader.isVisible = false
                            actionJob = false
                            toast(response.message)
                            if(response.status){
                                search(sessionManager.getWarehouse().toString())
                            }
                        }

                        override fun onError(anError: ANError) {
                            actionJob = false
                            binding.loader.isVisible = false
                            toast(anError.message + anError.errorDetail)
                        }
                    })
        }
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this@ReceiveActivity, ProfileActivity::class.java)
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

        binding.toolbar.setOnClickListener{
            //   drawer.openDrawer(GravityCompat.START)
            val intent = Intent(this@ReceiveActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }


}