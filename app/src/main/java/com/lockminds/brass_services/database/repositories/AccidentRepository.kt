package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.AccidentDao
import com.lockminds.brass_services.model.Accident
import kotlinx.coroutines.flow.Flow

class AccidentRepository(
     private val dao: AccidentDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(escorter: String, items: List<Accident>){
          dao.syncItems(escorter, items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(escorter: String) : Flow<List<Accident>> = dao.getItems(escorter)

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItem(itemId: String) : Accident = dao.getItem(itemId)

}