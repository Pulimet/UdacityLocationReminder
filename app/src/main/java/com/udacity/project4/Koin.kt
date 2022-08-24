package com.udacity.project4

import android.content.Context
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.login.LoginViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.navigation.NavObserver
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.GetResource
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
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
        singleOf(::GetResource)
        factoryOf(::NavObserver)

        single { LocalDB.createRemindersDao(androidContext()) }

        single<ReminderDataSource> { RemindersLocalRepository(get()) }
        viewModelOf(::NavViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::RemindersListViewModel)
        viewModelOf(::SaveReminderViewModel)
    }
}