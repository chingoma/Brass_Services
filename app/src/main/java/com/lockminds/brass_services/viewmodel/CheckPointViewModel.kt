package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.*
import com.lockminds.brass_services.database.repositories.CheckPointActionRepository
import com.lockminds.brass_services.database.repositories.CheckPointRepository
import com.lockminds.brass_services.model.CheckPoint
import com.lockminds.brass_services.model.CheckPointActions


class CheckPointViewModel(appRepository: CheckPointRepository) : ViewModel() {

    private val repository = appRepository

    fun getItems(): LiveData<List<CheckPoint>>{
        return repository.getItems().asLiveData()
    }

}

class CheckPointViewModelFactory(private val appRepository: CheckPointRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckPointViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckPointViewModel(
                    appRepository = appRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}