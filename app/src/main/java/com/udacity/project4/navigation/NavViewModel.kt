package com.udacity.project4.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import com.udacity.project4.utils.emitSharedFlow
import com.udacity.project4.utils.logD
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NavViewModel : ViewModel() {
    // Navigation Support
    private val _navigate = MutableSharedFlow<NavParams>()
    val getChangeNavigation = _navigate.asSharedFlow()
    private val _navigateUp = MutableSharedFlow<Unit>()
    val getNavigateUp = _navigateUp.asSharedFlow()

    fun navigateTo(navDirections: NavDirections, extras: FragmentNavigator.Extras? = null) {
        logD()
        viewModelScope.launch { _navigate.emit(NavParams(navDirections, extras)) }
    }

    fun navigateUp() {
        emitSharedFlow(_navigateUp)
    }
}