package com.lockminds.brass_services.database.repositories

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.mediator.OffloadLotRemoteMediator
import com.lockminds.brass_services.model.OffloadLot
import com.lockminds.brass_services.retrofit.OffloadLotService
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that works with local and remote data sources.
 */

class OffloadLotPagedRepository(
     private val service: OffloadLotService,
     private val database: AppDatabase
) {

     /**
      * Search repositories whose names match the query, exposed as a stream of data that will emit
      * every time we get more data from the network.
      */
     @ExperimentalPagingApi
     fun getItems(context: Context,query: String): Flow<PagingData<OffloadLot>> {

          val pagingSourceFactory = { database.offloadLotDao().getPagedItems(query) }

          return Pager(
               config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
               remoteMediator = OffloadLotRemoteMediator(
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
