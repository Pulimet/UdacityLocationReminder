package com.udacity.project4

import android.app.Application

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Koin.init(applicationContext)
    }
}