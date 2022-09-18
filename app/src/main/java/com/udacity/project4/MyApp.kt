package com.udacity.project4

import android.app.Application

class MyApp : Application() {

    // Code review fixes
    // TODO: Coroutine is not properly tested using the check_loading testing function.
    // TODO: No Espresso tests for Snackbar and Toast messages.

    override fun onCreate() {
        super.onCreate()
        Koin.init(applicationContext)
    }
}