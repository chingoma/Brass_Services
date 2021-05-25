/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lockminds.brass_services.mediator

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.lockminds.brass_services.SessionManager
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.model.Attendance
import com.lockminds.brass_services.model.AttendanceRemoteKeys
import com.lockminds.brass_services.retrofit.AttendanceService
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val GITHUB_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class AttendanceRemoteMediator(
    private val context: Context,
    private val query: String,
    private val service: AttendanceService,
    private val appDatabase: AppDatabase
) : RemoteMediator<Int, Attendance>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Attendance>): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                if (remoteKeys == null) {
                    // The LoadType is PREPEND so some data was loaded before,
                    // so we should have been able to get remote keys
                    // If the remoteKeys are null, then we're an invalid state and we have a bug
                    throw InvalidObjectException("Remote key and the prevKey should not be null")
                }
                // If the previous key is null, then we can't request more data
                val prevKey = remoteKeys.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys == null || remoteKeys.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }

        }

        val apiQuery = query

        try {
            val sessionManager = SessionManager(context)
            val apiResponse = service.getAttendance(token = "Bearer ${sessionManager.getLoginToken()}",apiQuery, page, state.config.pageSize)

            val repos = apiResponse.items
            val endOfPaginationReached = repos.isEmpty()
            appDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    appDatabase.attendanceDao().clearRemoteKeys()
                    appDatabase.attendanceDao().emptyTable()
                }
                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = repos.map {
                    AttendanceRemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                appDatabase.attendanceDao().insertKeys(keys)
                appDatabase.attendanceDao().insertItems(repos)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Attendance>): AttendanceRemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { repo ->
                // Get the remote keys of the last item retrieved
                appDatabase.attendanceDao().getRemoteKey(repo.id.toString())
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Attendance>): AttendanceRemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { repo ->
                // Get the remote keys of the first items retrieved
                appDatabase.attendanceDao().getRemoteKey(repo.id.toString())
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Attendance>
    ): AttendanceRemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                appDatabase.attendanceDao().getRemoteKey(repoId.toString())
            }
        }
    }

}