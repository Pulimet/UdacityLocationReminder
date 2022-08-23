package com.udacity.project4

import android.content.Context
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object Koin {
    fun init(applicationContext: Context) {
        startKoin {
            androidContext(applicationContext)
            modules(listOf(mainModule))
        }
    }

    private val mainModule = module {
        viewModelOf(::RemindersListViewModel)
        viewModelOf(::SaveReminderViewModel)
        singleOf(::RemindersLocalRepository)
        single { LocalDB.createRemindersDao(androidContext()) }
    }
}