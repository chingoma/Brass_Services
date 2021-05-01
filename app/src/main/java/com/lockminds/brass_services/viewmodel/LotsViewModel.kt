package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.LotRepository
import com.lockminds.brass_services.model.Lot


class LotsViewModel(val repo: LotRepository) : ViewModel() {

    private val repository = repo

    fun allLots(): LiveData<List<Lot>> = repository.getLots().asLiveData()

}

class LotsViewModelFactory(private val repo: LotRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LotsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LotsViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}