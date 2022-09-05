package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService.Companion.EXTRA_ARRAY_FENCE_ID
import com.udacity.project4.utils.GeofenceUtils
import com.udacity.project4.utils.GeofenceUtils.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.logD
import com.udacity.project4.utils.logE

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD()
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            GeofencingEvent.fromIntent(intent)?.also { onReceiveGeoEvent(it, context) }
        }
    }

    private fun onReceiveGeoEvent(geofencingEvent: GeofencingEvent, context: Context) {
        logD()
        if (geofencingEvent.hasError()) {
            logE(GeofenceUtils.errorMessage(context, geofencingEvent.errorCode))
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            logD(context.getString(R.string.geofence_entered))

            if (geofencingEvent.triggeringGeofences.isNullOrEmpty()) {
                logE("No Geofence Trigger Found!")
                return
            }
            geofencingEvent.triggeringGeofences?.let { geofenceList ->
                val fenceIdsList = geofenceList.map { it.requestId }
                val intent = Intent().apply {
                    putStringArrayListExtra(EXTRA_ARRAY_FENCE_ID, ArrayList(fenceIdsList))
                }
                GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
            }
        }
    }
}