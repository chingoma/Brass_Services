/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lockminds.brass_services.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.lockminds.brass_services.Constants.Companion.ACTION_GEOFENCE_EVENT
import com.lockminds.brass_services.Constants.Companion.LOCKMINDS_ACTION_GEOFENCE_EVENT
import com.lockminds.brass_services.Constants.Companion.LOCKMINDS_ACTION_GEOFENCE_EVENT_ENTERING
import com.lockminds.brass_services.Constants.Companion.LOCKMINDS_ACTION_GEOFENCE_EVENT_EXIT
import com.lockminds.brass_services.R


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == ACTION_GEOFENCE_EVENT) {

            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                val intentData = Intent()
                intentData.action = LOCKMINDS_ACTION_GEOFENCE_EVENT
                intentData.putExtra("fenceID", fenceId)
                intentData.putExtra("track_action", LOCKMINDS_ACTION_GEOFENCE_EVENT_ENTERING)
                context.sendBroadcast(intentData)


//                val notificationManager = ContextCompat.getSystemService(
//                    context,
//                    NotificationManager::class.java
//                ) as NotificationManager
//
//                notificationManager.sendGeofenceEnteredNotification(
//                    context
//                )
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                val intentData = Intent()
                intentData.action = LOCKMINDS_ACTION_GEOFENCE_EVENT
                intentData.putExtra("fenceID", fenceId)
                intentData.putExtra("track_action", LOCKMINDS_ACTION_GEOFENCE_EVENT_EXIT)
                context.sendBroadcast(intentData)


                val notificationManager = ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendGeofenceEnteredNotification(
                    context
                )
            }

        }
    }


}

private const val TAG = "GeofenceReceiver"
