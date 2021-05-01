package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.CheckPointHistoryRepository
import com.lockminds.brass_services.database.repositories.LotRepository
import com.lockminds.brass_services.model.CheckPointHistory
import com.lockminds.brass_services.model.Lot


class LotHistoryViewModel(val repo: CheckPointHistoryRepository) : ViewModel() {

    private val repository = repo

    fun getItems(lot:String): LiveData<List<CheckPointHistory>> = repository.getItems(lot).asLiveData()

}

class LotHistoryViewModelFactory(private val repo: CheckPointHistoryRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LotHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LotHistoryViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}