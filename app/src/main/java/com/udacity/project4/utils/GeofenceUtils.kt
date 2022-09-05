package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import java.util.concurrent.TimeUnit

object GeofenceUtils {

    const val ACTION_GEOFENCE_EVENT = "LocationReminder.action.ACTION_GEOFENCE_EVENT"
    private const val GEOFENCE_RADIUS_IN_METERS = 100f
    private val GEOFENCE_EXPIRATION_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(365)

    @SuppressLint("MissingPermission")
    fun addGeofencing(activity: Activity, reminderData: ReminderDTO) {
        logD("requestId: ${reminderData.id}")
        val geofencingRequest = createGeofencingRequest(reminderData)
        val geofencePendingIntent = getGeofencePendingIntent(activity)
        val geofencingClient = LocationServices.getGeofencingClient(activity)
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                logD("Add Geofence: ${geofencingRequest.geofences[0].requestId}")
            }
            addOnFailureListener {
                logW(it.message ?: "")
            }
        }
    }

    private fun createGeofencingRequest(reminderData: ReminderDTO): GeofencingRequest {
        // Build the Geofence Object
        val geofence = createGeofence(reminderData)
        // Build the geofence request
        return createGeofencingRequest(geofence)
    }

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private fun getGeofencePendingIntent(activity: Activity): PendingIntent {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        return PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createGeofence(reminderData: ReminderDTO) = Geofence.Builder()
        // Set the request ID, string to identify the geofence.
        .setRequestId(reminderData.id)
        // Set the circular region of this geofence.
        .setCircularRegion(
            reminderData.latitude ?: 0.0,
            reminderData.longitude ?: 0.0,
            GEOFENCE_RADIUS_IN_METERS
        )
        // Set the expiration duration of the geofence. This geofence gets
        // automatically removed after this period of time.
        .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
        // Set the transition types of interest. Alerts are only generated for these
        // transition. We track entry and exit transitions in this sample.
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()

    private fun createGeofencingRequest(geofence: Geofence) = GeofencingRequest.Builder()
        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

        // Add the geofences to be monitored by geofencing service.
        .addGeofence(geofence)
        .build()

    /**
     * Returns the error string for a geofencing error code.
     */
    fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
            )
            else -> resources.getString(R.string.unknown_geofence_error)
        }
    }

}