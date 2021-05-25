package com.lockminds.brass_services.database.repositories

import androidx.annotation.WorkerThread
import com.lockminds.brass_services.database.daos.AttendanceDao
import com.lockminds.brass_services.model.Attendance
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(
     private val dao: AttendanceDao
) {

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     suspend fun syncItems(items: List<Attendance>){
          dao.syncItems(items)
     }

     @Suppress("RedundantSuspendModifier")
     @WorkerThread
     fun getItems(escorter: String) : Flow<List<Attendance>> = dao.getItems(escorter)


}