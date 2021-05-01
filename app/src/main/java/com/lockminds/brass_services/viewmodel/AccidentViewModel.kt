package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.AccidentRepository
import com.lockminds.brass_services.model.Accident


class AccidentViewModel( val repo: AccidentRepository) : ViewModel() {

    private val repository = repo

    fun allItems(escorter: String): LiveData<List<Accident>> = repository.getItems(escorter).asLiveData()

}

class AccidentViewModelFactory(private val repo: AccidentRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccidentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccidentViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}