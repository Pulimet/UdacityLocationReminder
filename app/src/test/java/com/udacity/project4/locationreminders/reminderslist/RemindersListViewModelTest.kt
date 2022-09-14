package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.extensions.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var remindersRepository: FakeDataSource
    private lateinit var tasksViewModel: RemindersListViewModel

    private val rem1 = ReminderDTO("Title1", "Description1", "TA-1", 1.0, 1.0)
    private val rem2 = ReminderDTO("Title2", "Description2", "TA-2", 2.0, 2.0)
    private val rem3 = ReminderDTO("Title3", "Description3", "TA-3", 3.0, 3.0)


    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        remindersRepository.addReminders(rem1, rem2, rem3)
        tasksViewModel = RemindersListViewModel(remindersRepository)
    }

    @Test
    fun loadReminders() {
        // WHEN
        tasksViewModel.loadReminders()
        // THEN
        val value = tasksViewModel.remindersList.getOrAwaitValue()
        Assert.assertEquals(value[0], covertDataFromDbToUIForm(rem1))
        Assert.assertEquals(value[1], covertDataFromDbToUIForm(rem2))
        Assert.assertEquals(value[2], covertDataFromDbToUIForm(rem3))
    }

    private fun covertDataFromDbToUIForm(reminder: ReminderDTO): ReminderDataItem {
        return ReminderDataItem(
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.id
        )
    }


}