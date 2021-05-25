/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lockminds.brass_services.database.repositories

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.mediator.AttendanceRemoteMediator
import com.lockminds.brass_services.model.Attendance
import com.lockminds.brass_services.retrofit.AttendanceService
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that works with local and remote data sources.
 */

class AttendancePagedRepository(
     private val service: AttendanceService,
     private val database: AppDatabase
) {

     /**
      * Search repositories whose names match the query, exposed as a stream of data that will emit
      * every time we get more data from the network.
      */
     @ExperimentalPagingApi
     fun getItems(context: Context,query: String): Flow<PagingData<Attendance>> {

          val pagingSourceFactory = { database.attendanceDao().getPagedItems(query) }

          return Pager(
               config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
               remoteMediator = AttendanceRemoteMediator(
                    context,
                    query,
                    service,
                    database
               ),
               pagingSourceFactory = pagingSourceFactory
          ).flow
     }


     companion object {
          private const val NETWORK_PAGE_SIZE = 50
     }
}
