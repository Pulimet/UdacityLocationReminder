package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
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
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SelectLocationFragment : Fragment(),
    MenuProvider,
    OnMapReadyCallback,
    GoogleMap.OnPoiClickListener,
    GoogleMap.OnMapClickListener {

    val viewModel: SaveReminderViewModel by inject()
    private val navViewModel by sharedViewModel<NavViewModel>()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var latLng: LatLng? = null
    private var marker: Marker? = null
    private var circle: Circle? = null

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    handlePermissionsResult(true) // Precise location access granted.
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    handlePermissionsResult(true)// Only approximate location access granted.
                }
                else -> {
                    handlePermissionsResult(false)// No location access granted.
                }
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
        viewModel.onLocationSelected(latLng)
    }

    // OnMapReadyCallback
    override fun onMapReady(maps: GoogleMap) {
        map = maps.apply {
            setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            setOnPoiClickListener(this@SelectLocationFragment)
            setOnMapClickListener(this@SelectLocationFragment)
        }
        enableMyLocation()
    }

    // GoogleMap.OnPoiClickListener
    override fun onPoiClick(poi: PointOfInterest) {
        latLng = poi.latLng
        removeAndAddMarker(poi.latLng, poi.name)
        removeAndAddCircle(poi.latLng)
    }

    // GoogleMap.OnMapClickListener
    override fun onMapClick(latLng: LatLng) {
        this.latLng = latLng
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
            if (task.isSuccessful) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            task.result.latitude,
                            task.result.longitude
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
            isLocationPermissionGranted() -> handlePermissionsResult(true)
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                showDialogWithPermissionRationale()
            else -> requestPermission()
        }
    }

    private fun showDialogWithPermissionRationale() {
        AlertDialog.Builder(requireActivity()).apply {
            setMessage(getString(R.string.location_permission_rationale))
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                requestPermission()
            }
            create().show()
        }
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun handlePermissionsResult(granted: Boolean) {
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
