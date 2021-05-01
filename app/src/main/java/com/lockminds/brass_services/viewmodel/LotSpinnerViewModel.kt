package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.*
import com.lockminds.brass_services.database.repositories.LotRepository
import com.lockminds.brass_services.model.Lot


class LotSpinnerViewModel(appRepository: LotRepository) : ViewModel() {

    private val repository = appRepository

    fun getItems(): LiveData<List<Lot>>{
        return repository.getLots().asLiveData()
    }

}

class LotSpinnerViewModelFactory(private val appRepository: LotRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LotSpinnerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LotSpinnerViewModel(
                    appRepository = appRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}