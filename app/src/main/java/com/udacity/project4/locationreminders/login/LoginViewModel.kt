package com.udacity.project4.locationreminders.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.udacity.project4.R
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.GetResource
import com.udacity.project4.utils.emitSharedFlow
import com.udacity.project4.utils.logE
import com.udacity.project4.utils.logI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val navViewModel: NavViewModel, private val getResource: GetResource) : ViewModel() {

    private val _loginBtnText = MutableStateFlow(getResource.getString(R.string.login))
    val loginBtnText = _loginBtnText.asStateFlow()

    private val _launchSignInIntent = MutableSharedFlow<Intent>()
    val launchSignInIntent = _launchSignInIntent.asSharedFlow()

    private val _launchSignOut = MutableSharedFlow<Unit>()
    val launchSignOut = _launchSignOut.asSharedFlow()

    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setLogo(R.drawable.map)
        .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()))
        .build()

    fun onResume() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            logI("User not signed in")
        } else {
            logI("User already signed in ${currentUser.displayName}!")
            _loginBtnText.value = getResource.getString(R.string.logout)
        }
    }

    fun onBtnLoginOrLogoutClick() {
        viewModelScope.launch {
            if (Firebase.auth.currentUser == null) {
                _launchSignInIntent.emit(signInIntent)
            } else {
                emitSharedFlow(_launchSignOut)
            }
        }
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        if (result == null) {
            logE("Failed to login, result is null")
            return
        }

        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val displayName = FirebaseAuth.getInstance().currentUser?.displayName
                logI("Successfully signed in user $displayName!")
                navViewModel.navigateTo(LoginFragmentDirections.actionLoginFragmentToReminderListFragment())
            }
            Activity.RESULT_CANCELED -> logI("Sign in unsuccessful ${result.idpResponse?.error?.errorCode}")
            else -> logI("Sign in unsuccessful ${result.idpResponse?.error?.errorCode}")
        }
    }

    fun onLogoutCompleted() {
        logI("Logout completed!")
        _loginBtnText.value = getResource.getString(R.string.login)
    }
}