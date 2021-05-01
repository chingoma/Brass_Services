package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.CheckPointDao
import com.lockminds.brass_services.database.daos.CheckPointHistoryDao
import com.lockminds.brass_services.database.daos.LotDao
import com.lockminds.brass_services.model.CheckPoint
import com.lockminds.brass_services.model.CheckPointHistory
import com.lockminds.brass_services.model.Lot
import kotlinx.coroutines.flow.Flow

class CheckPointHistoryRepository(
     private val dao: CheckPointHistoryDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<CheckPointHistory>){
          dao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(lot: String) : Flow<List<CheckPointHistory>> = dao.getItems(lot)


}