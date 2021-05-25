package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.PermissionsRepository
import com.lockminds.brass_services.database.repositories.UserRepository
import com.lockminds.brass_services.model.Permissions
import com.lockminds.brass_services.model.User


class PermissionsViewModel( val repo: PermissionsRepository) : ViewModel() {

    private val repository = repo

    fun getPermissions(id: String): LiveData<Permissions> = repository.getPermissions(id).asLiveData()

    suspend fun syncPermissions(id: String, permissions: Permissions) = repository.syncPermissions(id, permissions)

}

class PermissionsViewModelFactory(private val repo: PermissionsRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionsViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}