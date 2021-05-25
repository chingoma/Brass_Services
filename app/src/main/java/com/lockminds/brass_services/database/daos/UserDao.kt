package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: String): Flow<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    fun deleteUser(id: String)

    @Transaction
    suspend fun syncUser(id: String, user: User) {
        deleteUser(id)
        insertUser(user)
    }

}