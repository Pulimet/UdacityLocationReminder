package com.udacity.project4.locationreminders.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentLoginBinding
import com.udacity.project4.utils.FragmentBinding
import com.udacity.project4.utils.collectIt
import org.koin.android.ext.android.inject

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {
    private val binding by FragmentBinding(FragmentLoginBinding::bind)
    private val loginViewModel: LoginViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModels()
        binding.btnLogin.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        loginViewModel.onResume()
    }

    private fun observeViewModels() {
        loginViewModel.apply {
            loginBtnText.collectIt(viewLifecycleOwner) { binding.btnLogin.text = it }
            launchSignInIntent.collectIt(viewLifecycleOwner) { signInLauncher.launch(it) }
            launchSignOut.collectIt(viewLifecycleOwner) { logout() }
        }
    }

    // View.OnClickListener
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> loginViewModel.onBtnLoginOrLogoutClick()
        }
    }

    // SignIn/Logout code
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
        loginViewModel.onSignInResult(result)
    }

    private fun logout() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                loginViewModel.onLogoutCompleted()
            }
    }
}