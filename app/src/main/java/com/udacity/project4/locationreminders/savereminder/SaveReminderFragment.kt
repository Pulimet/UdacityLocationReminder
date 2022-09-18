package com.udacity.project4.locationreminders.savereminder

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SaveReminderFragment : Fragment() {

    //Get the view model this time as a single to be shared with the another fragment
    private val viewModel by sharedViewModel<SaveReminderViewModel>()
    private val navViewModel by sharedViewModel<NavViewModel>()
    private lateinit var binding: FragmentSaveReminderBinding

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            PermissionUtils.printLog(permissions)
            checkPermissionsAndInvokeSaveReminderClick()
        }

    private val requestLocationSettingsOn =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                logD("Location on")
                viewModel.onSaveReminderClick()
            } else {
                logD("Location off")
                checkDeviceLocation(false)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        logD("ViewModel: $viewModel")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        binding.viewModel = viewModel
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navViewModel = navViewModel
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            viewModel.onSelectLocationClick()
        }
        binding.saveReminder.setOnClickListener { checkPermissionsAndInvokeSaveReminderClick() }

        observeViewModel()
    }

    // Location
    private fun checkPermissionsAndInvokeSaveReminderClick() {
        if (!PermissionUtils.isBackgroundPermissionGranted(requireContext())) {
            logW("Location permissions not granted")
            PermissionUtils.locationPermissionCheckFlow(
                requireActivity(),
                requestPermissions,
                permissionGranted = { checkDeviceLocation() }
            )
            return
        }
        logD("Location permissions granted")
        checkDeviceLocation()
    }

    private fun checkDeviceLocation(resolve: Boolean = true) {
        logD()
        LocationUtils.checkDeviceLocationSettings(
            requireActivity(),
            requestLocationSettingsOn,
            onLocationEnabled = { viewModel.onSaveReminderClick() },
            resolve
        )
    }

    private fun observeViewModel() {
        viewModel.apply {
            showErrorMessage.observe(viewLifecycleOwner) {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
            showToast.observe(viewLifecycleOwner) {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
            showSnackBar.observe(viewLifecycleOwner) {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            }
            showSnackBarInt.observe(viewLifecycleOwner) {
                Snackbar.make(requireView(), getString(it), Snackbar.LENGTH_LONG).show()
            }
            addGeofencing.observe(viewLifecycleOwner) {
                GeofenceUtils.addGeofencing(requireActivity(), it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        viewModel.onClear()
    }

}
