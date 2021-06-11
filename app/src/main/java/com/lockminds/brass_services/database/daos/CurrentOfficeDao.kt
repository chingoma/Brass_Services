package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.CurrentOffice
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentOfficeDao {

    @Query("SELECT * FROM current_office WHERE user_id = :user AND id = :office")
    fun getOffice(user: String, office: String): Flow<CurrentOffice>

    @Query("SELECT * FROM current_office WHERE user_id = :user")
    fun currentOffice(user: String): Flow<CurrentOffice>

    @Query("SELECT * FROM current_office WHERE user_id = :user")
    fun getList(user: String): List<CurrentOffice>

    @Query("SELECT * FROM current_office WHERE user_id = :user")
    fun getItems(user: String): Flow<List<CurrentOffice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<CurrentOffice>)

    @Query("DELETE FROM current_office WHERE user_id = :user")
    fun emptyTable(user: String)

    @Transaction
    fun syncItems(user: String,items: List<CurrentOffice>) {
        // Anything inside this method runs in a single transaction.
        emptyTable(user)
        insertItems(items)
    }

}