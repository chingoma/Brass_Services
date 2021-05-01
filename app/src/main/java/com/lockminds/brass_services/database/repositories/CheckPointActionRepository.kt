package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.CheckPointActionDao
import com.lockminds.brass_services.database.daos.CheckPointDao
import com.lockminds.brass_services.database.daos.LotDao
import com.lockminds.brass_services.model.CheckPoint
import com.lockminds.brass_services.model.CheckPointActions
import com.lockminds.brass_services.model.Lot
import kotlinx.coroutines.flow.Flow

class CheckPointActionRepository(
     private val dao: CheckPointActionDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<CheckPointActions>){
          dao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems() : Flow<List<CheckPointActions>> = dao.getItems()


}