package com.lockminds.brass_services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.lockminds.brass_services.database.repositories.UserRepository
import com.lockminds.brass_services.model.User


class UserViewModel( val repo: UserRepository) : ViewModel() {

    private val repository = repo

    fun getUser(id: String): LiveData<User> = repository.getUser(id).asLiveData()

    suspend fun syncUser(id: String, user: User) = repository.syncUser(id, user)

}

class UserViewModelFactory(private val repo: UserRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(
                    repo = repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}