package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    private val reminderData = ReminderDTO("Title3", "Description3", "TA-3", 3.0, 3.0)

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest {
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(reminderData)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(reminderData.id)

        // THEN - The loaded data contains the expected values.
        MatcherAssert.assertThat(loaded as ReminderDTO, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(loaded.id, CoreMatchers.`is`(reminderData.id))
        MatcherAssert.assertThat(loaded.title, CoreMatchers.`is`(reminderData.title))
        MatcherAssert.assertThat(loaded.description, CoreMatchers.`is`(reminderData.description))
        MatcherAssert.assertThat(loaded.location, CoreMatchers.`is`(reminderData.location))
        MatcherAssert.assertThat(loaded.latitude, CoreMatchers.`is`(reminderData.latitude))
        MatcherAssert.assertThat(loaded.longitude, CoreMatchers.`is`(reminderData.longitude))
    }
    @Test
    fun whenReminderNotExistGetError() = runTest {
        // WHEN - Get the task by id from the database. When task is not exist in DB.
        val loaded = database.reminderDao().getReminderById(reminderData.id)

        // THEN - The loaded data contains null
        MatcherAssert.assertThat(loaded, CoreMatchers.nullValue())
    }
}