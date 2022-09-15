package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.navigation.NavParams
import com.udacity.project4.navigation.NavViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    companion object {
        private const val ERROR_MESSAGE = "Error Test"
    }

    @Rule
    @JvmField
    var mInstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var appContext: Application

    private val rem1 = ReminderDTO("Title1", "Description1", "TA-1", 1.0, 1.0)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            single<ReminderDataSource> { FakeDataSource() }
            single { RemindersListViewModel(get()) }
            singleOf(::NavViewModel)
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun whenRepoHasReminderFragmentShowsIt() = runTest {
        val fakeDataSource: ReminderDataSource = get()
        fakeDataSource.saveReminder(rem1)

        // GIVEN - On the home screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // WHEN + THEN
        Espresso.onView(withId(R.id.reminderssRecyclerView))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(rem1.title))))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(rem1.description))))
    }

    @Test
    fun whenAddTasksClickedNavigatesSaveReminder() = runTest {
        // GIVEN - On the home screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navViewModel: NavViewModel = get()
        val navParamsList = mutableListOf<NavParams>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            navViewModel.getChangeNavigation.collect {
                navParamsList.add(it)
            }
        }

        // WHEN
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN
        assertEquals(1, navParamsList.size)
        assertEquals(ReminderListFragmentDirections.toSaveReminder(), navParamsList[0].navDirections)

        collectJob.cancel()
    }

    @Test
    fun whenErrorMessageSentItShown() = runTest {
        // GIVEN - On the home screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val remindersListViewModel: RemindersListViewModel = get()
        val errorMessagesList = mutableListOf<String>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            remindersListViewModel.showErrorMessage.observeForever {
                errorMessagesList.add(it)
            }
        }

        // WHEN
        withContext(Dispatchers.Main) {
            remindersListViewModel.showErrorMessage.postValue(ERROR_MESSAGE)
        }

        // THEN
        assertEquals(1, errorMessagesList.size)
        assertEquals(ERROR_MESSAGE, errorMessagesList[0])

        collectJob.cancel()
    }


}