package com.lockminds.brass_services

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.lockminds.brass_services.LotDetailsActivity.Companion.createIntent
import com.lockminds.brass_services.adapter.LotsAdapter
import com.lockminds.brass_services.databinding.ActivityMainBinding
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.model.Lot
import com.lockminds.brass_services.ui.AccidentsActivity
import com.lockminds.brass_services.ui.LotsActivity
import com.lockminds.brass_services.ui.OffloadActivity
import com.lockminds.brass_services.ui.ReceiveActivity
import com.lockminds.brass_services.viewmodel.AccidentViewModel
import com.lockminds.brass_services.viewmodel.AccidentViewModelFactory
import com.lockminds.brass_services.viewmodel.LotsViewModel
import com.lockminds.brass_services.viewmodel.LotsViewModelFactory
import com.lockminds.libs.constants.APIURLs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    private val lotsViewModel by viewModels<LotsViewModel> {
        LotsViewModelFactory((application as App).lotRepo)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View  = binding.root
        setContentView(view)
        initComponent()
        initNavigationMenu()
        binding.lytNoConnection.isVisible = true
        setAdapter()
        userViewModel.getUser(sessionManager.getUserId().toString()).observe(this){
            it?.let {
                binding.toolbar.title = it.name
                binding.toolbar.subtitle = it.reg_number
            }
        }

        permissionsViewModel.getPermissions(sessionManager.getUserId().toString()).observe(this){
            it?.let {permissions ->
                binding.receiver.isVisible = permissions.receive!! > 0
                binding.intro.isVisible = permissions.escort!! > 0
            }
        }
    }

    override fun onResume() {
        super.onResume()
        syncAccidents()
        syncLots()
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
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        binding.lotQty.setOnClickListener {
            val intent = Intent(this@MainActivity, LotsActivity::class.java)
            startActivity(intent)
        }

        binding.receive.setOnClickListener {
            val intent = Intent(this@MainActivity, ReceiveActivity::class.java)
            startActivity(intent)
        }

        binding.offload.setOnClickListener {
            val intent = Intent(this@MainActivity, OffloadActivity::class.java)
            startActivity(intent)
        }

        binding.reportAccident.setOnClickListener {
            val intent = Intent(this@MainActivity, AccidentsActivity::class.java)
            startActivity(intent)
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

       val lotsAdapter = LotsAdapter(this) { lot -> lotAdapterOnClickNear(
            lot
        ) }

        lotsViewModel.allLots().observe(this){

            it?.let {
                lotsAdapter.submitList(it)
                if(it.isNotEmpty()){
                    binding.lytNoConnection.isVisible = false
                }
                binding.lytNoConnection.isVisible = false
            }
        }


        binding.recyclerView.adapter = lotsAdapter

    }

    private fun syncLots(){
        AndroidNetworking.get(APIURLs.BASE_URL + "lots/get_all_lots")
            .setPriority(Priority.HIGH)
            .setPriority(Priority.LOW)
            .build()
            .getAsObjectList(Lot::class.java, object : ParsedRequestListener<List<Lot>> {
                override fun onResponse(list: List<Lot>) {
                    val items: List<Lot> = list
                    GlobalScope.launch {
                        (application as App).lotRepo.syncItems(items)
                    }
                }
                override fun onError(anError: ANError) {
                    Toast.makeText(this@MainActivity, anError.errorDetail, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun syncAccidents(){
        AndroidNetworking.get(APIURLs.BASE_URL + "get_accidents")
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

                    override fun onError(anError: ANError) { }
                })
    }

    private fun lotAdapterOnClickNear(lot: Lot) {
        startActivity(createIntent(this, lot))
    }

}