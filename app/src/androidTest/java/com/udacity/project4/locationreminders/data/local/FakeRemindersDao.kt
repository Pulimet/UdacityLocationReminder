package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class FakeRemindersDao : RemindersDao {

    private var remindersList: ArrayList<ReminderDTO> = arrayListOf()

    override suspend fun getReminders(): List<ReminderDTO> {
        return remindersList
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        return remindersList.find { it.id == reminderId }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }
}