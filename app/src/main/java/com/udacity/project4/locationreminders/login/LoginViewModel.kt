package com.udacity.project4.locationreminders.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.utils.logE
import com.udacity.project4.utils.logI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _launchSignInIntent = MutableSharedFlow<Intent>()
    val launchSignInIntent = _launchSignInIntent.asSharedFlow()

    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()))
        .build()

    fun onBtnLoginClick() {
        viewModelScope.launch {
            _launchSignInIntent.emit(signInIntent)
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
            }
            Activity.RESULT_CANCELED -> logI("Sign in unsuccessful ${result.idpResponse?.error?.errorCode}")
        }
    }


}