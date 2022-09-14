package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.extensions.getOrAwaitValue
import com.udacity.project4.navigation.NavViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var remindersRepository: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private val reminderData = ReminderDTO("Title3", "Description3", "TA-3", 3.0, 3.0)

    private val mockApplication = Mockito.mock(Application::class.java)

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(mockApplication, remindersRepository).apply {
            navViewModel = NavViewModel()
            reminderTitle.value = reminderData.title
            reminderDescription.value = reminderData.description
            reminderSelectedLocationStr.value = reminderData.location
            latitude.value = reminderData.latitude
            longitude.value = reminderData.longitude
        }
    }

    @Test
    fun onSaveReminderClick_updates_geofencing_value() {
        // WHEN
        saveReminderViewModel.onSaveReminderClick()
        // THEN
        val value = saveReminderViewModel.addGeofencing.getOrAwaitValue()
        Assert.assertEquals(value.title, reminderData.title)
        Assert.assertEquals(value.description, reminderData.description)
        Assert.assertEquals(value.location, reminderData.location)
        Assert.assertEquals(value.longitude, reminderData.longitude)
        Assert.assertEquals(value.latitude, reminderData.latitude)
    }

}