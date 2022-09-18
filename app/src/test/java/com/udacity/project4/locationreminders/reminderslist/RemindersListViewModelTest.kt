package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.extensions.covertDataFromDbToUIForm
import com.udacity.project4.locationreminders.extensions.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersRepository: FakeDataSource
    private lateinit var remindersViewModel: RemindersListViewModel

    private val rem1 = ReminderDTO("Title1", "Description1", "TA-1", 1.0, 1.0)
    private val rem2 = ReminderDTO("Title2", "Description2", "TA-2", 2.0, 2.0)
    private val rem3 = ReminderDTO("Title3", "Description3", "TA-3", 3.0, 3.0)


    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        remindersRepository.addReminders(rem1, rem2, rem3)
        remindersViewModel = RemindersListViewModel(remindersRepository)
    }

    @Test
    fun loadReminders_onSuccess_updatesReminderList() {
        // WHEN
        remindersViewModel.loadReminders()
        // THEN
        val value = remindersViewModel.remindersList.getOrAwaitValue()
        Assert.assertEquals(value[0], covertDataFromDbToUIForm(rem1))
        Assert.assertEquals(value[1], covertDataFromDbToUIForm(rem2))
        Assert.assertEquals(value[2], covertDataFromDbToUIForm(rem3))
    }

    @Test
    fun loadReminders_onRequest_showsLoading_collected() = runTest {
        val loadingStatesList = mutableListOf<Boolean>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            remindersViewModel.showLoading.observeForever {
                loadingStatesList.add(it)
            }
        }
        // WHEN
        remindersRepository.setReturnError(true)
        remindersViewModel.loadReminders()

        // THEN
        Assert.assertEquals(2, loadingStatesList.size)
        Assert.assertEquals(true, loadingStatesList[0])
        Assert.assertEquals(false, loadingStatesList[1])

        collectJob.cancel()
    }



    @Test
    fun loadReminders_onError_showsSnackBar() {
        // WHEN
        remindersRepository.setReturnError(true)
        remindersViewModel.loadReminders()
        // THEN
        val value = remindersViewModel.showSnackBar.getOrAwaitValue()
        Assert.assertEquals(value, "Error")
    }
}