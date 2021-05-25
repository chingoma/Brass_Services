package com.lockminds.brass_services.database.daos

import androidx.room.*
import com.lockminds.brass_services.model.Permissions
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionDao {

    @Query("SELECT * FROM permissions WHERE id = :id")
    fun getPermissions(id: String): Flow<Permissions>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPermissions(permissions: Permissions)

    @Query("DELETE FROM permissions WHERE id = :id")
    fun deletePermissions(id: String)

    @Transaction
    suspend fun syncPermissions(id: String, permissions: Permissions) {
        deletePermissions(id)
        insertPermissions(permissions)
    }

}