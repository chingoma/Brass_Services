package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.OffloadLotDao
import com.lockminds.brass_services.model.OffloadLot
import kotlinx.coroutines.flow.Flow

class OffloadLotRepository(
     private val dao: OffloadLotDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<OffloadLot>){
          dao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(escorter: String) : Flow<List<OffloadLot>> = dao.getItems(escorter)

}