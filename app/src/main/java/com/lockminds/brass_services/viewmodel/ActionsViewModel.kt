package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.*
import com.lockminds.brass_services.database.repositories.CheckPointActionRepository
import com.lockminds.brass_services.model.CheckPointActions


class ActionsViewModel(appRepository: CheckPointActionRepository) : ViewModel() {

    private val repository = appRepository

    fun getItems(): LiveData<List<CheckPointActions>>{
        return repository.getItems().asLiveData()
    }

}

class ActionsViewModelFactory(private val appRepository: CheckPointActionRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActionsViewModel(
                    appRepository = appRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}