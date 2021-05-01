package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.Lot
import kotlinx.coroutines.flow.Flow

@Dao
interface LotDao {

    @Query("SELECT * FROM lots")
    fun getLots(): Flow<List<Lot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<Lot>)

    @Query("DELETE FROM lots")
    fun emptyTable()

    @Transaction
    fun syncItems(items: List<Lot>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }
}