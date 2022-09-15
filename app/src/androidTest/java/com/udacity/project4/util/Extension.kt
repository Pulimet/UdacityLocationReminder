package com.udacity.project4.util

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

/**
 * Wait for view to be visible
 */
fun ViewInteraction.waitUntilVisible(timeout: Long = 1000): ViewInteraction {
    val startTime = System.currentTimeMillis()
    val endTime = startTime + timeout

    do {
        try {
            check(matches(isDisplayed()))
            return this
        } catch (e: AssertionFailedError) {
            Thread.sleep(50)
        }
    } while (System.currentTimeMillis() < endTime)

    throw TimeoutException()
}


fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun getDescription(): String {
            return "wait for " + delay + "milliseconds"
        }

        override fun perform(uiController: UiController, view: View?) {
            uiController.loopMainThreadForAtLeast(delay)
        }
    }
}

fun clickXY(x: Int, y: Int): ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)
            val screenX = (screenPos[0] + x).toFloat()
            val screenY = (screenPos[1] + y).toFloat()
            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        0,
        0
    )
}