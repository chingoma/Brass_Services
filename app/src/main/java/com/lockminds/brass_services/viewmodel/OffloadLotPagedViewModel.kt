package com.lockminds.brass_services.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.lockminds.brass_services.database.repositories.OffloadLotPagedRepository
import com.lockminds.brass_services.model.OffloadLot
import kotlinx.coroutines.flow.Flow

class OffloadLotPagedViewModel(private val repository: OffloadLotPagedRepository) : ViewModel() {
    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<OffloadLot>>? = null


    @ExperimentalPagingApi
    fun getItems(context: Context,queryString: String): Flow<PagingData<OffloadLot>> {

        currentQueryValue = queryString

        val newResult: Flow<PagingData<OffloadLot>> = repository.getItems (context,
            queryString
        )
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

}


class OffloadLotPagedViewModelFactory(private val repository: OffloadLotPagedRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OffloadLotPagedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OffloadLotPagedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}