package com.udacity.project4.locationreminders.login

import androidx.fragment.app.Fragment
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentLoginBinding
import com.udacity.project4.utils.FragmentBinding
import org.koin.android.ext.android.inject

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val binding by FragmentBinding(FragmentLoginBinding::bind)
    private val viewModel: LoginViewModel by inject()
}