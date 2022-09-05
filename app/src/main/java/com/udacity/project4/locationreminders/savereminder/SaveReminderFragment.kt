package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.GeofenceUtils
import com.udacity.project4.utils.logD
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SaveReminderFragment : Fragment() {

    //Get the view model this time as a single to be shared with the another fragment
    private val viewModel by sharedViewModel<SaveReminderViewModel>()
    private val navViewModel by sharedViewModel<NavViewModel>()
    private lateinit var binding: FragmentSaveReminderBinding

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
        binding.saveReminder.setOnClickListener {
            viewModel.onSaveReminderClick()
        }

        observeViewModel()
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
