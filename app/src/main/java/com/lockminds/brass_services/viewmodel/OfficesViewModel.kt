package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.OfficeRepository
import com.lockminds.brass_services.model.Office


class OfficesViewModel( val repo: OfficeRepository) : ViewModel() {

    private val repository = repo

    fun allItems(escorter: String): LiveData<List<Office>> = repository.getItems().asLiveData()

    fun getOffice(office: String): Office = repository.getOffice(office)

}

class OfficesViewModelFactory(private val repo: OfficeRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfficesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OfficesViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}