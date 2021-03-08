package com.lockminds.brass_services

import android.os.Bundle
import android.view.View
import com.lockminds.brass_services.databinding.ActivityUpdateLotDestinationBinding

class UpdateLotDestinationActivity : BaseActivity() {

    lateinit var binding: ActivityUpdateLotDestinationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateLotDestinationBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        toolbarBackNavigation()
    }

}