package com.lockminds.brass_services.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.lockminds.brass_services.database.repositories.AttendancePagedRepository
import com.lockminds.brass_services.model.Attendance
import kotlinx.coroutines.flow.Flow

class AttendancePagedViewModel(private val repository: AttendancePagedRepository) : ViewModel() {
    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Attendance>>? = null


    @ExperimentalPagingApi
    fun getItems(context: Context,queryString: String): Flow<PagingData<Attendance>> {
        val lastResult = currentSearchResult
//        if (queryString == currentQueryValue && lastResult != null) {
//            return lastResult
//        }
        currentQueryValue = queryString

        val newResult: Flow<PagingData<Attendance>> = repository.getItems (context,
            queryString
        )
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

}


class AttendancePagedViewModelFactory(private val repository: AttendancePagedRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendancePagedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendancePagedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}