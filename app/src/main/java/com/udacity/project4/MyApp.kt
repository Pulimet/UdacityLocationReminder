package com.udacity.project4

import android.app.Application

class MyApp : Application() {

    // Code review fixes
    // TODO: Coroutine is not properly tested using the check_loading testing function.

    override fun onCreate() {
        super.onCreate()
        Koin.init(applicationContext)
    }
}