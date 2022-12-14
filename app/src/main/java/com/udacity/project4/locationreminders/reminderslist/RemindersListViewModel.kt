package com.udacity.project4.locationreminders.reminderslist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.SingleLiveEvent
import com.udacity.project4.utils.logD
import kotlinx.coroutines.launch

class RemindersListViewModel(private val dataSource: ReminderDataSource) : ViewModel() {
    lateinit var navViewModel: NavViewModel

    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()

    fun onAddReminderFabClick() {
        logD()
        navViewModel.navigateTo(ReminderListFragmentDirections.toSaveReminder())
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        viewModelScope.launch {
            showLoading.value = true
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.value = false
            when (result) {
                is Result.Success<*> -> onResultSuccess(result)
                is Result.Error -> showSnackBar.value = result.message ?: ""
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    private fun onResultSuccess(result: Result.Success<*>) {
        val reminderDataItemList = covertDataFromDbToUIForm(result)
        remindersList.value = ArrayList<ReminderDataItem>().apply { addAll(reminderDataItemList) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun covertDataFromDbToUIForm(result: Result.Success<*>): List<ReminderDataItem> {
        val reminderDTOList = result.data as List<ReminderDTO>
        return reminderDTOList.map { reminder ->
            //map the reminder data from the DB to the be ready to be displayed on the UI
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id
            )
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    fun onLogoutComplete() {
        navViewModel.navigateTo(ReminderListFragmentDirections.actionReminderListFragmentToLoginFragment())
    }
}