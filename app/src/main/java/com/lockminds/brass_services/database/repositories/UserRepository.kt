package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.UserDao
import com.lockminds.brass_services.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
     private val dao: UserDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncUser(id: String, user: User){
          dao.syncUser(id, user)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getUser(id: String) : Flow<User> = dao.getUser(id)

}