package com.udacity.project4

import android.content.Context
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.login.LoginViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

object Koin {
    fun init(applicationContext: Context) {
        startKoin {
            androidContext(applicationContext)
            modules(listOf(mainModule))
        }
    }

    private val mainModule = module {
        single { LocalDB.createRemindersDao(androidContext()) }

        single<ReminderDataSource> { RemindersLocalRepository(get()) }
        viewModelOf(::RemindersListViewModel)
        viewModelOf(::SaveReminderViewModel)
        viewModelOf(::LoginViewModel)
    }
}