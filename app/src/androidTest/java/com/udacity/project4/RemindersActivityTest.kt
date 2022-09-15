package com.udacity.project4

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.login.LoginViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.navigation.NavObserver
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.util.*
import com.udacity.project4.utils.GetResource
import com.udacity.project4.utils.logD
import com.udacity.project4.utils.test.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {

    @Rule
    @JvmField
    var mInstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

    private val reminderData = ReminderDTO("Title3", "Description3", "TA-3", 3.0, 3.0)

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            single { GetResource(appContext) }
            factoryOf(::NavObserver)
            singleOf(::RemindersListViewModel)
            viewModelOf(::NavViewModel)
            viewModelOf(::LoginViewModel)
            viewModelOf(::SaveReminderViewModel)
            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun ifRepoEmptyNoDataShown() = runBlockingAndActivityScenarioControl {
        Espresso.onView(withId(R.id.noDataTextView)).check(matches(withText(R.string.no_data)))
    }

    @Test
    fun whenReminderAddedItShown() = runBlockingAndActivityScenarioControl {
        val viewModel = get<RemindersListViewModel>()
        // WHEN
        repository.saveReminder(reminderData)
        viewModel.loadReminders()
        // THEN
        viewModel.showNoData.getOrAwaitValue()
        onView(isRoot()).perform(waitFor(1000))
        onView(withId(R.id.noDataTextView)).check(matches(not(isCompletelyDisplayed())))
    }

    @Test
    fun x3() = runBlockingAndActivityScenarioControl {}

    @Test
    fun x4() = runBlockingAndActivityScenarioControl {}

    @Test
    fun x5() = runBlockingAndActivityScenarioControl {}

    @Test
    fun x6() = runBlockingAndActivityScenarioControl {}

    @Test
    fun x7() = runBlockingAndActivityScenarioControl {}

    private fun runBlockingAndActivityScenarioControl(function: suspend () -> Unit) = runTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario) // LOOK HERE
        function()
        activityScenario.close()
    }


}
