package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.AccidentDao
import com.lockminds.brass_services.database.daos.AccidentGalleryDao
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.model.AccidentGallery
import kotlinx.coroutines.flow.Flow

class AccidentGalleryRepository(
     private val dao: AccidentGalleryDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(lot: String, items: List<AccidentGallery>){
          dao.syncItems(lot, items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(lotId: String) : Flow<List<AccidentGallery>> = dao.getItems(lotId)


}