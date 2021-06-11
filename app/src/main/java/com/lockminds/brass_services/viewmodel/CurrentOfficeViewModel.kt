package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.CurrentOfficeRepository
import com.lockminds.brass_services.model.CurrentOffice


class CurrentOfficeViewModel( val repo: CurrentOfficeRepository) : ViewModel() {

    private val repository = repo

    fun allItems(user: String): LiveData<List<CurrentOffice>> = repository.getItems(user).asLiveData()

    fun currentOffice(user: String) : LiveData<CurrentOffice> = repository.currentOffice(user).asLiveData()

}

class CurrentOfficeViewModelFactory(private val repo: CurrentOfficeRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentOfficeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrentOfficeViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}