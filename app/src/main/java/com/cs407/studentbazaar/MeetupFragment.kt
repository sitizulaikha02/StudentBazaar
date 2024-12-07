package com.cs407.studentbazaar

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Locale
import android.location.Geocoder

class MeetupFragment : Fragment() {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private var mDestinationLatLng: LatLng? = null

    private lateinit var editTextLocation: EditText
    private lateinit var buttonSetLocation: Button
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

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
        progressBar = view.findViewById(R.id.loading_indicator)

        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap
            checkLocationPermissionAndGetCurrentLocation()
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Set click listener for the 'Set' button
        buttonSetLocation.setOnClickListener {
            val locationName = editTextLocation.text.toString().trim()
            if (locationName.isNotEmpty()) {
                geocodeLocation(locationName)
            } else {
                Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun geocodeLocation(locationName: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        showLoadingIndicator(true)
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                mDestinationLatLng = LatLng(address.latitude, address.longitude)
                updateMapWithDestination()
            } else {
                Toast.makeText(context, "Location not found. Try a different address.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("GeocodeError", "Error during geocoding: ${e.message}", e)
            Toast.makeText(context, "Error finding location. Please check your network.", Toast.LENGTH_SHORT).show()
        } finally {
            showLoadingIndicator(false)
        }
    }

    private fun updateMapWithDestination() {
        mMap.clear()
        mDestinationLatLng?.let { destination ->
            setLocationMarker(destination, "Meetup Location")
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
                getCurrentLocationAndDrawPolyline()
            }
        }
    }

    private fun getCurrentLocationAndDrawPolyline() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null && mDestinationLatLng != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    setLocationMarker(currentLatLng, "Your Location")
                    drawPolyline(currentLatLng, mDestinationLatLng!!)
                } else {
                    Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error fetching location: ${it.message}", Toast.LENGTH_SHORT).show()
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
        updateCameraBounds(start, end)
    }

    private fun updateCameraBounds(start: LatLng, end: LatLng) {
        val bounds = LatLngBounds.Builder()
            .include(start)
            .include(end)
            .build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun showLoadingIndicator(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
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
}
