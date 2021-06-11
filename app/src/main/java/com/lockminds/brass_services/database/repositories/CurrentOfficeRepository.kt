package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.CurrentOfficeDao
import com.lockminds.brass_services.database.daos.OfficesDao
import com.lockminds.brass_services.model.CurrentOffice
import com.lockminds.brass_services.model.Office
import kotlinx.coroutines.flow.Flow

class CurrentOfficeRepository(
     private val dao: CurrentOfficeDao
) {


     @WorkerThread
     fun syncItems(user: String, items: List<CurrentOffice>){
          dao.syncItems(user, items)
     }

     @WorkerThread
     fun getItems(user: String) : Flow<List<CurrentOffice>> = dao.getItems(user)

     @WorkerThread
     fun getList(user: String) : List<CurrentOffice> = dao.getList(user)

     @WorkerThread
     fun currentOffice(user: String) : Flow<CurrentOffice> = dao.currentOffice(user)

     @WorkerThread
     fun getOffice(user: String, office:String) : Flow<CurrentOffice> = dao.getOffice(user, office)


}