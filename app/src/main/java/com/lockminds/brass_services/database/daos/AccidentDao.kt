package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.Accident
import kotlinx.coroutines.flow.Flow

@Dao
interface AccidentDao {

    @Query("SELECT * FROM accidents WHERE id= :itemId")
    fun getItem(itemId: String): Accident

    @Query("SELECT * FROM accidents WHERE escorter_id= :escorter")
    fun getItems(escorter: String): Flow<List<Accident>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<Accident>)

    @Query("DELETE FROM accidents WHERE escorter_id= :escorter")
    fun emptyTable(escorter: String)

    @Transaction
    fun syncItems(escorter: String, items: List<Accident>) {
        // Anything inside this method runs in a single transaction.
        emptyTable(escorter)
        insertItems(items)
    }
}