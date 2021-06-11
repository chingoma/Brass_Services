package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.Reasons
import kotlinx.coroutines.flow.Flow

@Dao
interface ReasonsDao {

    @Query("SELECT * FROM reasons WHERE team_id = :team")
    fun getItems(team: String): Flow<List<Reasons>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(orders: List<Reasons>)

    @Query("DELETE FROM reasons WHERE team_id = :team")
    fun emptyTable(team: String)

    @Transaction
    fun syncItems(team: String, items: List<Reasons>) {
        // Anything inside this method runs in a single transaction.
        emptyTable(team)
        insertItems(items)
    }
}