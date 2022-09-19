package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
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
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SelectLocationFragment : Fragment(),
    MenuProvider,
    OnMapReadyCallback,
    GoogleMap.OnPoiClickListener,
    GoogleMap.OnMapClickListener {

    private val viewModel by sharedViewModel<SaveReminderViewModel>()
    private val navViewModel by sharedViewModel<NavViewModel>()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private var circle: Circle? = null

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            PermissionUtils.printLog(permissions)
            checkPermissionsAndEnableMyLocation()
        }

    private val requestLocationSettingsOn =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                logD("Location on")
            } else {
                logD("Location off")
                checkDeviceLocation(false)
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
        checkPermissionsAndEnableMyLocation()
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
    private fun checkPermissionsAndEnableMyLocation() {
        if (!PermissionUtils.isForegroundLocationPermissionGranted(requireContext())) {
            logW("Location permissions not granted")
            PermissionUtils.foregroundLocationPermissionCheckFlow(
                requireActivity(),
                requestPermissions,
                permissionGranted = { checkDeviceLocation() }
            )
            return
        }
        logD("Location permissions granted")
        checkDeviceLocation()
    }

    private fun checkDeviceLocation(resolve: Boolean = true) {
        logD()
        enableMapMyLocation()
        LocationUtils.checkDeviceLocationSettings(
            requireActivity(),
            requestLocationSettingsOn,
            onLocationEnabled = { },
            resolve
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMapMyLocation() {
        logD()
        map.isMyLocationEnabled = true
        moveCameraToCurrentLocation()
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
