package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.PermissionDao
import com.lockminds.brass_services.database.daos.UserDao
import com.lockminds.brass_services.model.Permissions
import com.lockminds.brass_services.model.User
import kotlinx.coroutines.flow.Flow

class PermissionsRepository(
     private val dao: PermissionDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncPermissions(id: String, permissions: Permissions){
          dao.syncPermissions(id,permissions)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getPermissions(id: String) : Flow<Permissions> = dao.getPermissions(id)

}