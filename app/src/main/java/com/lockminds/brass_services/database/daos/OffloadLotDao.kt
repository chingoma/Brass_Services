package com.lockminds.brass_services.database.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.lockminds.brass_services.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OffloadLotDao {

    @Query("DELETE FROM offload_lot_remote_keys")
    suspend fun clearRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(orders: List<ReceiverLotRemoteKeys>)

    @Query("SELECT * FROM offload_lot_remote_keys WHERE id = :id ORDER BY id DESC")
    suspend fun getRemoteKey(id: String): OffloadLotRemoteKeys

    @Query("SELECT * FROM offload_lots WHERE warehouse_id = :id ORDER BY id DESC")
    fun getList(id: String): List<OffloadLot>

    @Query("SELECT * FROM offload_lots WHERE warehouse_id = :id ORDER BY id DESC")
    fun getItems(id: String): Flow<List<OffloadLot>>

    @Query("SELECT * FROM offload_lots WHERE warehouse_id = :id ORDER BY id DESC")
    fun getPagedItems(id: String): PagingSource<Int, OffloadLot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<OffloadLot>)

    @Query("DELETE FROM offload_lots")
    fun emptyTable()

    @Transaction
    suspend fun syncItems(items: List<OffloadLot>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }
}