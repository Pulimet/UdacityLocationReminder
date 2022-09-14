package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private val observableRemindersTasks = MutableLiveData<Result<List<ReminderDTO>>>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        runBlocking {
            observableRemindersTasks.value = Result.Success(reminders.asList())
        }
    }

    override suspend fun getReminders() = if (shouldReturnError) {
        Result.Error("Error", 404)
    } else {
        observableRemindersTasks.value!!
    }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        observableRemindersTasks.value = Result.Success(listOf(reminder))
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
    }


}