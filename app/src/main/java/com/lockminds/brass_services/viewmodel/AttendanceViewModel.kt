package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.AttendanceRepository
import com.lockminds.brass_services.model.Attendance


class AttendanceViewModel(private val repo: AttendanceRepository) : ViewModel() {

    private val repository = repo

    fun allItems(escorter: String): LiveData<List<Attendance>> = repository.getItems(escorter).asLiveData()

}

class AttendanceViewModelFactory(private val repo: AttendanceRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}