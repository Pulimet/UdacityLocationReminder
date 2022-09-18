package com.udacity.project4.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.udacity.project4.R

object PermissionUtils {

    fun foregroundPermissionCheckFlow(
        activity: Activity,
        requestPermissions: ActivityResultLauncher<Array<String>>,
        permissionGranted: () -> Unit
    ) {
        logW("")
        when {
            isForegroundLocationPermissionGranted(activity) -> permissionGranted()
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                showDialogWithPermissionRationale(activity, requestPermissions)
            else -> requestForegroundPermission(requestPermissions)
        }
    }

    fun isForegroundLocationPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


    private fun showDialogWithPermissionRationale(activity: Activity, requestPermissions: ActivityResultLauncher<Array<String>>) {
        logD()
        AlertDialog.Builder(activity).apply {
            setMessage(activity.getString(R.string.location_permission_rationale))
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                requestForegroundPermission(requestPermissions)
            }
            create().show()
        }
    }

    private fun requestForegroundPermission(requestPermissions: ActivityResultLauncher<Array<String>>) {
        logD("-")
        requestPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun printLog(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        logD("permissions.size: ${permissions.size}")
        permissions.forEach {
            logD("${it.key} : ${it.value}")
        }
    }

    // private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    /*
        @TargetApi(29)
    private val requestBackground =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            logPermissions(permissions)
            val isBGLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false)
            handleBackgroundPermissionsResult(isBGLocationGranted || !runningQOrLater)
        }
    */

    /*
        @TargetApi(29)
    private fun isLocationPermissionGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || !runningQOrLater)
    */

/*    @TargetApi(29)
    fun requestBackgroundPermission() {
        logD()
        requestBackground.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }*/
/*
    fun handleBackgroundPermissionsResult(granted: Boolean) {
        logD("granted: $granted")
        if (granted) {
            enableMyLocation()
        } else {
            showDialogWithPermissionRationale()
        }
    }*/

}