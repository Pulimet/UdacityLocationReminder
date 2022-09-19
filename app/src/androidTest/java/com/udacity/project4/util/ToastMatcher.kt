package com.udacity.project4.util

import android.os.IBinder

import android.view.WindowManager
import androidx.test.espresso.Root
import com.udacity.project4.utils.logD
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


class ToastMatcher : TypeSafeMatcher<Root>() {

    override fun matchesSafely(root: Root): Boolean {
        logD("====")
        val type: Int = root.windowLayoutParams.get().type
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            val windowToken: IBinder = root.decorView.windowToken
            val appToken: IBinder = root.decorView.applicationWindowToken
            if (windowToken === appToken) {
                return true
            }
        }
        logD("Return False: type: $type")
        return false
    }

    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

}