package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.AccidentGalleryRepository
import com.lockminds.brass_services.model.AccidentGallery


class AccidentGalleryViewModel( repo: AccidentGalleryRepository) : ViewModel() {

    private val repository = repo

    fun allItems(lotId: String): LiveData<List<AccidentGallery>> = repository.getItems(lotId).asLiveData()

}

class AccidentGalleryViewModelFactory(private val repo: AccidentGalleryRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccidentGalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccidentGalleryViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}