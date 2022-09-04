package com.udacity.project4.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.navigation.NavObserver
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.NotificationsUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RemindersActivity : AppCompatActivity(), NavObserver.Provider {

    private val navViewModel: NavViewModel by viewModel()
    private val navObserver: NavObserver by inject { parametersOf(this) }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        navObserver.observe()
        NotificationsUtils.createChannel(this)
    }

    override fun onStart() {
        super.onStart()
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // NavObserver.Provider
    override fun provideActivity() = this
    override fun provideNavController() = navController
    override fun provideNavViewModel() = navViewModel
}
