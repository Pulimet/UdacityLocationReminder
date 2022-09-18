package com.udacity.project4

import android.app.Application

class MyApp : Application() {

    // Code review fixes
    // TODO: The background location permission is requested in the select location screen (Nexus 5X, API 29).
    // TODO: Device location and required permissions aren't checked or properly handled right before adding a geofence (Nexus 5X, API 29).
    // TODO: All the users' reminders added earlier stop working after a certain amount of time.
    // TODO: Coroutine is not properly tested using the check_loading testing function.
    // TODO: Error handling is not properly done in the FakeDataSource class.
    // TODO: No Espresso tests for Snackbar and Toast messages.
    // TODO: Idling resources are not properly used.

    override fun onCreate() {
        super.onCreate()
        Koin.init(applicationContext)
    }
}