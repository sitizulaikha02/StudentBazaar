package com.cs407.studentbazaar

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import android.location.Geocoder
import android.widget.Button
import android.widget.EditText
import java.util.Locale


class MeetupFragment : Fragment() {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private var mDestinationLatLng: LatLng? = null

    private lateinit var editTextLocation: EditText
    private lateinit var buttonSetLocation: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_meetup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        editTextLocation = view.findViewById(R.id.editText_location)
        buttonSetLocation = view.findViewById(R.id.button_set_location)

        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap

            // Check location permission
            checkLocationPermissionAndGetCurrentLocation()
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Set click listener for the 'Set' button
        buttonSetLocation.setOnClickListener {
            val locationName = editTextLocation.text.toString()
            if (locationName.isNotEmpty()) {
                geocodeLocation(locationName)
            } else {
                Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun geocodeLocation(locationName: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                mDestinationLatLng = LatLng(address.latitude, address.longitude)
                updateMapWithDestination()
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error finding location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapWithDestination() {
        mMap.clear()
        mDestinationLatLng?.let { destination ->
            // Add marker at the destination
            setLocationMarker(destination, "Meetup Location")

            // Draw polyline from current location to destination
            getCurrentLocationAndDrawPolyline()
        }
    }

    private fun setLocationMarker(location: LatLng, title: String) {
        val markerOptions = MarkerOptions().position(location).title(title)
        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun checkLocationPermissionAndGetCurrentLocation() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission granted
                getCurrentLocationAndDrawPolyline()
            }
        }
    }

    private fun getCurrentLocationAndDrawPolyline() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return
            }

            mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null && mDestinationLatLng != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    // Add marker for current location
                    setLocationMarker(currentLatLng, "Your Location")
                    // Draw polyline
                    drawPolyline(currentLatLng, mDestinationLatLng!!)
                } else {
                    Log.e("LocationError", "Current location or destination is null")
                    Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun drawPolyline(start: LatLng, end: LatLng) {
        val polylineOptions = PolylineOptions()
            .add(start)
            .add(end)
            .color(android.graphics.Color.RED)
            .width(5f)
        mMap.addPolyline(polylineOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocationAndDrawPolyline()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }



}
