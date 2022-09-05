package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.navigation.NavViewModel
import com.udacity.project4.utils.logD
import com.udacity.project4.utils.logW
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SelectLocationFragment : Fragment(),
    MenuProvider,
    OnMapReadyCallback,
    GoogleMap.OnPoiClickListener,
    GoogleMap.OnMapClickListener {

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val viewModel by sharedViewModel<SaveReminderViewModel>()
    private val navViewModel by sharedViewModel<NavViewModel>()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private var circle: Circle? = null

    private val requestForeground =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            logPermissions(permissions)
            val isFineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val isCoarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            handleForegroundPermissionsResult(isCoarseLocationGranted || isFineLocationGranted)
        }

    @TargetApi(29)
    private val requestBackground =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            logPermissions(permissions)
            val isBGLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false)
            handleBackgroundPermissionsResult(isBGLocationGranted || !runningQOrLater)
        }

    private fun logPermissions(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        logD("permissions.size: ${permissions.size}")
        permissions.forEach {
            logD("${it.key} : ${it.value}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        logD("ViewModel: $viewModel")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navViewModel = navViewModel
        setupMenu()
        setupMap()
        binding.saveLocation.setOnClickListener {
            onLocationSelected()
        }
        Snackbar.make(view, getString(R.string.select_poi_please), Snackbar.LENGTH_SHORT).show()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupMap() {
        binding.map.getFragment<SupportMapFragment>().getMapAsync(this)
    }

    private fun onLocationSelected() {
        viewModel.onLocationSelected()
    }

    // OnMapReadyCallback
    override fun onMapReady(maps: GoogleMap) {
        logD()
        map = maps.apply {
            setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            setOnPoiClickListener(this@SelectLocationFragment)
            setOnMapClickListener(this@SelectLocationFragment)
        }
        enableMyLocation()
    }

    // GoogleMap.OnPoiClickListener
    override fun onPoiClick(poi: PointOfInterest) {
        viewModel.updateLatLng(poi.latLng)
        viewModel.updatePoi(poi)
        removeAndAddMarker(poi.latLng, poi.name)
        removeAndAddCircle(poi.latLng)
    }

    // GoogleMap.OnMapClickListener
    override fun onMapClick(latLng: LatLng) {
        viewModel.updateLatLng(latLng)
        removeAndAddMarker(latLng)
        removeAndAddCircle(latLng)
    }

    private fun removeAndAddMarker(latLng: LatLng, poiName: String = "") {
        val name = if (poiName.isNotEmpty()) ": $poiName" else ""
        marker?.remove()
        marker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.selected_position) + name)
        )?.apply {
            showInfoWindow()
        }
    }

    private fun removeAndAddCircle(latLng: LatLng) {
        circle?.remove()
        circle = map.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(120.0)
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.circle_stroke))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.circle_fill))
        )
    }

    // Location
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isLocationPermissionGranted()) {
            logD("Location permissions granted")
            map.isMyLocationEnabled = true
            moveCameraToCurrentLocation()
        } else {
            logW("Location permissions not granted")
            permissionCheckFlow()
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToCurrentLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful && task.result != null) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            task.result?.latitude ?: 0.0,
                            task.result?.longitude ?: 0.0
                        ), 15f
                    )
                )
            }
        }
    }

    // Permissions
    private fun permissionCheckFlow() {
        logW("")
        when {
            isLocationPermissionGranted() -> handleForegroundPermissionsResult(true)
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                showDialogWithPermissionRationale()
            runningQOrLater && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ->
                showDialogWithPermissionRationale()
            else -> requestForegroungdPermission()
        }
    }

    private fun showDialogWithPermissionRationale() {
        logD()
        AlertDialog.Builder(requireActivity()).apply {
            setMessage(getString(R.string.location_permission_rationale))
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                requestForegroungdPermission()
            }
            create().show()
        }
    }

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

    private fun requestForegroungdPermission() {
        logD()
        val arrayOfPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestForeground.launch(arrayOfPermissions)
    }

    @TargetApi(29)
    private fun requestBackgroundPermission() {
        logD()
        requestBackground.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }

    private fun handleForegroundPermissionsResult(granted: Boolean) {
        logD("granted: $granted")
        if (granted) {
            if (runningQOrLater) {
                requestBackgroundPermission()
            } else {
                enableMyLocation()
            }
        } else {
            showDialogWithPermissionRationale()
        }
    }

    private fun handleBackgroundPermissionsResult(granted: Boolean) {
        logD("granted: $granted")
        if (granted) {
            enableMyLocation()
        } else {
            showDialogWithPermissionRationale()
        }
    }

    // MenuProvider
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.map_options, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.normal_map -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            else -> return false
        }
        return true
    }
}
