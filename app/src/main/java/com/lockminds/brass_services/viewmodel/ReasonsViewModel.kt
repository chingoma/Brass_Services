package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.*
import com.lockminds.brass_services.database.repositories.ReasonsRepository
import com.lockminds.brass_services.model.Reasons


class ReasonsViewModel(appRepository: ReasonsRepository) : ViewModel() {

    private val repository = appRepository

    fun getItems(team: String): LiveData<List<Reasons>>{
        return repository.getItems(team).asLiveData()
    }

}

class ReasonsViewModelFactory(private val appRepository: ReasonsRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReasonsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReasonsViewModel(
                    appRepository = appRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}