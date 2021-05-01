package com.lockminds.brass_services.ui

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.lockminds.brass_services.*
import com.lockminds.brass_services.adapter.LotsAdapter
import com.lockminds.brass_services.databinding.ActivityLotsBinding
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.viewmodel.LotsViewModel
import com.lockminds.brass_services.viewmodel.LotsViewModelFactory
import com.lockminds.libs.constants.APIURLs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LotsActivity : BaseActivity() {

    private val lotsViewModel by viewModels<LotsViewModel> {
        LotsViewModelFactory((application as App).lotRepo)
    }
    lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityLotsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLotsBinding.inflate(layoutInflater)
        val view:View = binding.root
        setContentView(view)

        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        Tools.setNavigationBarColor(this, R.color.colorPrimaryDark)
        sessionManager = SessionManager(applicationContext)
        initComponent()
        binding.lytNoConnection.isVisible = true
        setAdapter()
        syncLots()
    }

    private fun initComponent(){
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark)
        binding.toolbar.title = sessionManager.getName()
        binding.toolbar.subtitle = "Lot List"

        Glide
            .with(applicationContext)
            .load(sessionManager.getPhotoUrl())
            .centerCrop()
            .placeholder(R.mipmap.ic_launcher_round)
            .into(binding.image)
            //        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //            override fun onQueryTextSubmit(query: String?): Boolean {
            //                // filter recycler view when query submitted
            //                lotsAdapter?.filter?.filter(query)
            //                return false
            //            }
            //
            //            override fun onQueryTextChange(query: String?): Boolean {
            //                // filter recycler view when text is changed
            //                lotsAdapter?.filter?.filter(query)
            //                return false
            //            }
            //        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Tools.changeMenuIconColor(menu, resources.getColor(R.color.colorOnPrimary))
        return true
    }

    private fun setAdapter() {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        val lotsAdapter = LotsAdapter(this) { lot -> lotAdapterOnClickNear(
            lot
        ) }

        lotsViewModel.allLots().observe(this){

            it?.let {
                lotsAdapter.submitList(it)
                binding.lytNoConnection.isVisible = false
            }
        }

        binding.recyclerView.adapter = lotsAdapter

    }

    private fun syncLots(){
        AndroidNetworking.get(APIURLs.BASE_URL + "lots/get_all_lots")
            .setTag("lots")
            .addHeaders("accept", "application/json")
            .addHeaders("Authorization", "Bearer " + sessionManager.getLoginToken())
            .setPriority(Priority.HIGH)
            .setPriority(Priority.LOW)
            .build()
            .getAsObjectList(Lot::class.java, object : ParsedRequestListener<List<Lot>> {
                override fun onResponse(list: List<Lot>) {
                    val items: List<Lot> = list
                    if (items.isNotEmpty()) {
                        GlobalScope.launch {
                            (application as App).lotRepo.syncItems(items)
                        }

                    }
                }
                override fun onError(anError: ANError) {
                    Toast.makeText(this@LotsActivity, anError.errorDetail, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun lotAdapterOnClickNear(lot: Lot) {
        startActivity(LotDetailsActivity.createIntent(this, lot))
    }

}