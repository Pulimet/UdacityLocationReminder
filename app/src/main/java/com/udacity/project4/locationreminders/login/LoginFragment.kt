package com.udacity.project4.locationreminders.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentLoginBinding
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.FragmentBinding
import com.udacity.project4.utils.collectIt
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {
    private val binding by FragmentBinding(FragmentLoginBinding::bind)
    private val viewModel by viewModel<LoginViewModel>()
    private val navViewModel by sharedViewModel<NavViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navViewModel = navViewModel
        observeViewModels()
        binding.btnLogin.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun observeViewModels() {
        viewModel.apply {
            loginBtnText.collectIt(viewLifecycleOwner) { binding.btnLogin.text = it }
            launchSignInIntent.collectIt(viewLifecycleOwner) { signInLauncher.launch(it) }
            launchSignOut.collectIt(viewLifecycleOwner) { logout() }
        }
    }

    // View.OnClickListener
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> viewModel.onBtnLoginOrLogoutClick()
        }
    }

    // SignIn/Logout code
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
        viewModel.onSignInResult(result)
    }

    private fun logout() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                viewModel.onLogoutCompleted()
            }
    }
}