package com.lockminds.brass_services.database.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.lockminds.brass_services.model.Attendance
import com.lockminds.brass_services.model.AttendanceRemoteKeys
import com.lockminds.brass_services.model.Office
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("DELETE FROM attendance_remote_keys")
    suspend fun clearRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(orders: List<AttendanceRemoteKeys>)

    @Query("SELECT * FROM attendance_remote_keys WHERE id = :id ORDER BY id DESC")
    suspend fun getRemoteKey(id: String): AttendanceRemoteKeys

    @Query("SELECT * FROM attendances WHERE user_id = :id ORDER BY id DESC")
    fun getList(id: String): List<Attendance>

    @Query("SELECT * FROM attendances WHERE user_id = :id ORDER BY id DESC")
    fun getItems(id: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendances WHERE user_id = :id ORDER BY id DESC")
    fun getPagedItems(id: String): PagingSource<Int, Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<Attendance>)

    @Query("DELETE FROM attendances")
    fun emptyTable()

    @Transaction
    suspend fun syncItems(items: List<Attendance>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }

}