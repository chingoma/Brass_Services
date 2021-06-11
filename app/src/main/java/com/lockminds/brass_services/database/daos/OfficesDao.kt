package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.Office
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficesDao {

    @Query("SELECT * FROM offices")
    fun getList(): List<Office>

    @Query("SELECT * FROM offices WHERE id = :office")
    fun getOffice(office: String): Office

    @Query("SELECT * FROM offices")
    fun getItems(): Flow<List<Office>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<Office>)

    @Query("DELETE FROM offices")
    fun emptyTable()

    @Transaction
    fun syncItems(items: List<Office>) {
        // Anything inside this method runs in a single transaction.
        emptyTable()
        insertItems(items)
    }

}