package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.OfficesDao
import com.lockminds.brass_services.model.Office
import kotlinx.coroutines.flow.Flow

class OfficeRepository(
     private val dao: OfficesDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<Office>){
          dao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems() : Flow<List<Office>> = dao.getItems()

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getList() : List<Office> = dao.getList()


}