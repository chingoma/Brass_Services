package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.ReasonsDao
import com.lockminds.brass_services.model.Reasons
import kotlinx.coroutines.flow.Flow

class ReasonsRepository(
     private val dao: ReasonsDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(team: String, items: List<Reasons>){
          dao.syncItems(team,items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(team: String) : Flow<List<Reasons>> = dao.getItems(team)

}