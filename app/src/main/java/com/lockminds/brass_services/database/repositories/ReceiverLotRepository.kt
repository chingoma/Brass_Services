package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.ReceiverLotDao
import com.lockminds.brass_services.model.ReceiverLot
import kotlinx.coroutines.flow.Flow

class ReceiverLotRepository(
     private val dao: ReceiverLotDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<ReceiverLot>){
          dao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(escorter: String) : Flow<List<ReceiverLot>> = dao.getItems(escorter)

}