package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.Accident
import com.lockminds.brass_services.model.AccidentGallery
import kotlinx.coroutines.flow.Flow

@Dao
interface AccidentGalleryDao {

    @Query("SELECT * FROM accident_Galleries WHERE accident_id = :lotId")
    fun getItems(lotId: String): Flow<List<AccidentGallery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<AccidentGallery>)

    @Query("DELETE FROM accident_Galleries WHERE accident_id = :lotId")
    fun emptyTable(lotId: String)

    @Transaction
    fun syncItems(escorter: String, items: List<AccidentGallery>) {
        emptyTable(escorter)
        insertItems(items)
    }
}