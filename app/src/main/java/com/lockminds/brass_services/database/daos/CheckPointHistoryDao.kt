package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.CheckPointHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckPointHistoryDao {
    @Query("SELECT * FROM check_point_history WHERE lot_no = :lot")
    fun getItems(lot: String): Flow<List<CheckPointHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<CheckPointHistory>)

    @Query("DELETE FROM check_point_history")
    fun emptyTable()

    @Transaction
    fun syncItems(items: List<CheckPointHistory>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }
}