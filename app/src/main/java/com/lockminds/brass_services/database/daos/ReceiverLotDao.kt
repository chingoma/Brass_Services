package com.lockminds.brass_services.database.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.lockminds.brass_services.model.Attendance
import com.lockminds.brass_services.model.AttendanceRemoteKeys
import com.lockminds.brass_services.model.ReceiverLot
import com.lockminds.brass_services.model.ReceiverLotRemoteKeys
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiverLotDao {

    @Query("DELETE FROM receiver_lot_remote_keys")
    suspend fun clearRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(orders: List<ReceiverLotRemoteKeys>)

    @Query("SELECT * FROM receiver_lot_remote_keys WHERE id = :id ORDER BY id DESC")
    suspend fun getRemoteKey(id: String): ReceiverLotRemoteKeys

    @Query("SELECT * FROM receiver_lots WHERE warehouse_id = :id ORDER BY id DESC")
    fun getList(id: String): List<ReceiverLot>

    @Query("SELECT * FROM receiver_lots WHERE warehouse_id = :id ORDER BY id DESC")
    fun getItems(id: String): Flow<List<ReceiverLot>>

    @Query("SELECT * FROM receiver_lots WHERE warehouse_id = :id ORDER BY id DESC")
    fun getPagedItems(id: String): PagingSource<Int, ReceiverLot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<ReceiverLot>)

    @Query("DELETE FROM receiver_lots")
    fun emptyTable()

    @Transaction
    suspend fun syncItems(items: List<ReceiverLot>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }
}