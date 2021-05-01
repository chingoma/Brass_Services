package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.CheckPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckPointDao {

    @Query("SELECT * FROM check_points")
    fun getItems(): Flow<List<CheckPoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<CheckPoint>)

    @Query("DELETE FROM check_points")
    fun emptyTable()

    @Transaction
    fun syncItems(items: List<CheckPoint>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }

}