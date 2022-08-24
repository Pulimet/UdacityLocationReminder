package com.udacity.project4.utils

import android.content.Context
import androidx.annotation.StringRes

class GetResource(private val context: Context) {
    fun getString(@StringRes resId: Int) = context.getString(resId)
}