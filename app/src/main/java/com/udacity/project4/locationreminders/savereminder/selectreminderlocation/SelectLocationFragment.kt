package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.logD
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : Fragment(), MenuProvider, OnMapReadyCallback {

    val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            handlePermissionsResult(isGranted)
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
        setupMenu()
        setupMap()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupMap() {
        binding.map.getFragment<SupportMapFragment>().getMapAsync(this)
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }

    // OnMapReadyCallback
    override fun onMapReady(maps: GoogleMap) {
        map = maps
        enableMyLocation()
    }

    // Location
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            moveCameraToCurrentLocation()
        } else {
            requestPermission()
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
    private fun requestPermission() {
        activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun handlePermissionsResult(granted: Boolean) {
        if (granted) {
            enableMyLocation()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
