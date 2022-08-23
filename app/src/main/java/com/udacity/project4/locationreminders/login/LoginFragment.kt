package com.udacity.project4.locationreminders.login

import androidx.fragment.app.Fragment
import com.udacity.project4.R
import org.koin.android.ext.android.inject

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: LoginViewModel by inject()
}