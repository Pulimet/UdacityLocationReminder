package com.udacity.project4.locationreminders.data.local

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private val observableRemindersTasks = MutableLiveData<Result<List<ReminderDTO>>>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders() = if (shouldReturnError) {
        Result.Error("Error", 404)
    } else {
        observableRemindersTasks.value ?: Result.Success(emptyList())
    }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        observableRemindersTasks.value = Result.Success(listOf(reminder))
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("No result")
        }
        if (observableRemindersTasks.value is Result.Success) {
            val list = (observableRemindersTasks.value as Result.Success).data
            list.forEach {
                if (it.id == id) {
                    return Result.Success(it)
                }
            }
        }
        return Result.Error("No result")
    }

    override suspend fun deleteAllReminders() {
        observableRemindersTasks.value = Result.Success(emptyList())
    }
}