package com.udacity.project4

import android.app.Application

class MyApp : Application() {

    // Code review fixes
    // TODO: All the users' reminders added earlier stop working after a certain amount of time.
    // TODO: Coroutine is not properly tested using the check_loading testing function.
    // TODO: Error handling is not properly done in the FakeDataSource class.
    // TODO: No Espresso tests for Snackbar and Toast messages.

    override fun onCreate() {
        super.onCreate()
        Koin.init(applicationContext)
    }
}