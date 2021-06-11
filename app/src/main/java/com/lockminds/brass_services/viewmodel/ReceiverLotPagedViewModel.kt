package com.lockminds.brass_services.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.lockminds.brass_services.database.repositories.ReceiverLotPagedRepository
import com.lockminds.brass_services.model.ReceiverLot
import kotlinx.coroutines.flow.Flow

class ReceiverLotPagedViewModel(private val repository: ReceiverLotPagedRepository) : ViewModel() {
    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<ReceiverLot>>? = null


    @ExperimentalPagingApi
    fun getItems(context: Context,queryString: String): Flow<PagingData<ReceiverLot>> {

        currentQueryValue = queryString

        val newResult: Flow<PagingData<ReceiverLot>> = repository.getItems (context,
            queryString
        )
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

}


class ReceiverLotPagedViewModelFactory(private val repository: ReceiverLotPagedRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceiverLotPagedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReceiverLotPagedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}