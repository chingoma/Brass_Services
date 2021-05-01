package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.LotDao
import com.lockminds.brass_services.model.Lot
import kotlinx.coroutines.flow.Flow

class LotRepository(
     private val lotDao: LotDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<Lot>){
          lotDao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getLots() : Flow<List<Lot>> = lotDao.getLots()


}