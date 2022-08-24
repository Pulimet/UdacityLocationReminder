package com.udacity.project4.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.udacity.project4.utils.collectIt
import com.udacity.project4.utils.logE

class NavObserver(private val provider: Provider) {

    fun observe() {
        observeFragmentNavigation()
    }

    private fun observeFragmentNavigation() {
        provider.provideNavViewModel().apply {
            getChangeNavigation.collectIt(provider.provideActivity()) { navParams ->
                try {
                    navigateTo(navParams)
                } catch (e: IllegalArgumentException) {
                    logE(t = e)
                }
            }
            getNavigateUp.collectIt(provider.provideActivity()) {
                provider.provideNavController().navigateUp()
            }
        }
    }

    private fun navigateTo(navParams: NavParams) {
        if (navParams.navDirections == null) return
        provider.provideNavController().navigate(
            navParams.navDirections.actionId,
            navParams.navDirections.arguments,
            navParams.navOptions,
            navParams.extras
        )
    }


    interface Provider {
        fun provideActivity(): AppCompatActivity
        fun provideNavController(): NavController
        fun provideNavViewModel(): NavViewModel
    }
}