package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.SingleLiveEvent
import com.udacity.project4.utils.logD
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) : ViewModel() {
    lateinit var navViewModel: NavViewModel

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    val addGeofencing: SingleLiveEvent<ReminderDTO> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()


    fun onSelectLocationClick() {
        navViewModel.navigateTo(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    fun onSaveReminderClick() {
        logD()
        validateAndSaveReminder(
            ReminderDataItem(
                reminderTitle.value,
                reminderDescription.value,
                reminderSelectedLocationStr.value,
                latitude.value,
                longitude.value
            )
        )
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    private fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminderToDb(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminderToDb(reminderData: ReminderDataItem) {
        showLoading.value = true
        val reminderDTO = ReminderDTO(
            reminderData.title,
            reminderData.description,
            reminderData.location,
            reminderData.latitude,
            reminderData.longitude,
            reminderData.id
        )
        viewModelScope.launch {
            dataSource.saveReminder(reminderDTO)
            addGeofencing.value = reminderDTO
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navViewModel.navigateUp()
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.description.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_description
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    fun onLocationSelected() {
        if (latitude.value != null && latitude.value != null) {
            navViewModel.navigateUp()
        }
    }

    fun updateLatLng(latLng: LatLng) {
        latitude.value = latLng.latitude
        longitude.value = latLng.longitude
        val latStr = latLng.latitude.toString().substring(0, 8)
        val longStr = latLng.longitude.toString().substring(0, 8)
        reminderSelectedLocationStr.value = "$latStr : $longStr"
    }

    fun updatePoi(poi: PointOfInterest) {
        selectedPOI.value = poi
        reminderSelectedLocationStr.value = poi.name
    }
}