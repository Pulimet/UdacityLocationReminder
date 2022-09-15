package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private val rem1 = ReminderDTO("Title1", "Description1", "TA-1", 1.0, 1.0)
    private val rem2 = ReminderDTO("Title2", "Description2", "TA-2", 2.0, 2.0)
    private val rem3 = ReminderDTO("Title3", "Description3", "TA-3", 3.0, 3.0)

    @Before
    fun prepareLocalRepository() {
        remindersLocalRepository = RemindersLocalRepository(FakeRemindersDao(), Dispatchers.Main)
    }

    @Test
    fun saveAndGetReminder() = runTest {
        remindersLocalRepository.saveReminder(rem1)
        val reminder = remindersLocalRepository.getReminder(rem1.id)

        MatcherAssert.assertThat("Saved and loaded reminders are equal", rem1.equals(reminder))
    }

    @Test
    fun getReminders() = runTest {
        remindersLocalRepository.saveReminder(rem1)
        remindersLocalRepository.saveReminder(rem2)
        remindersLocalRepository.saveReminder(rem3)

        when (val result = remindersLocalRepository.getReminders()) {
            is Result.Error -> Assert.fail()
            is Result.Success -> Assert.assertEquals(3, result.data.size)
        }
    }

    @Test
    fun deleteAllReminders() = runTest {
        remindersLocalRepository.saveReminder(rem1)
        remindersLocalRepository.deleteAllReminders()

        when (val result = remindersLocalRepository.getReminders()) {
            is Result.Error -> Assert.fail()
            is Result.Success -> Assert.assertEquals(0, result.data.size)
        }
    }

}