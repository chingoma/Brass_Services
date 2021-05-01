package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.CheckPointActions
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckPointActionDao {

    @Query("SELECT * FROM check_point_actions")
    fun getItems(): Flow<List<CheckPointActions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<CheckPointActions>)

    @Query("DELETE FROM check_point_actions")
    fun emptyTable()

    @Transaction
    fun syncItems(items: List<CheckPointActions>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }
}