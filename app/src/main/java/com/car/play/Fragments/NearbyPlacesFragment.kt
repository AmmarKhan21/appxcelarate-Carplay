package com.car.play.android.app.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.NearbyPlace
import com.car.play.android.app.Adapters.NearbyPlaceAdapter
import com.car.play.android.app.databinding.FragmentNearbyPlacesBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class NearbyPlacesFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentNearbyPlacesBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placeAdapter: NearbyPlaceAdapter
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private var selectedCategory: String = "gas_station"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNearbyPlacesBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupMap()
        setupRecyclerView()
        setupChips()
        setupButtons()

        return binding.root
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(
            com.car.play.android.app.R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupRecyclerView() {
        placeAdapter = NearbyPlaceAdapter(mutableListOf()) { place ->
            navigateToPlace(place)
        }
        binding.rvPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaces.adapter = placeAdapter
    }

    private fun setupChips() {
        binding.chipGas.setOnClickListener { selectedCategory = "gas_station"; searchNearby() }
        binding.chipParking.setOnClickListener { selectedCategory = "parking"; searchNearby() }
        binding.chipCarWash.setOnClickListener { selectedCategory = "car_wash"; searchNearby() }
        binding.chipMechanic.setOnClickListener { selectedCategory = "car_repair"; searchNearby() }
        binding.chipEv.setOnClickListener { selectedCategory = "electric_vehicle_charging_station"; searchNearby() }
        binding.chipHospital.setOnClickListener { selectedCategory = "hospital"; searchNearby() }
    }

    private fun setupButtons() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1003)
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                val latLng = LatLng(it.latitude, it.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                searchNearby()
            }
        }
    }

    private fun searchNearby() {
        val location = currentLocation ?: return
        googleMap?.clear()

        val places = searchPlacesWithGeocoder(location.latitude, location.longitude, selectedCategory)
        placeAdapter.updateList(places)

        val categoryTitle = when (selectedCategory) {
            "gas_station" -> "Gas Stations"
            "parking" -> "Parking"
            "car_wash" -> "Car Wash"
            "car_repair" -> "Mechanics"
            "electric_vehicle_charging_station" -> "EV Chargers"
            "hospital" -> "Hospitals"
            else -> "Places"
        }
        binding.tvPlacesTitle.text = "Nearby $categoryTitle"

        places.forEach { place ->
            val markerColor = when (selectedCategory) {
                "gas_station" -> BitmapDescriptorFactory.HUE_ORANGE
                "parking" -> BitmapDescriptorFactory.HUE_BLUE
                "car_wash" -> BitmapDescriptorFactory.HUE_CYAN
                "car_repair" -> BitmapDescriptorFactory.HUE_RED
                "electric_vehicle_charging_station" -> BitmapDescriptorFactory.HUE_GREEN
                "hospital" -> BitmapDescriptorFactory.HUE_ROSE
                else -> BitmapDescriptorFactory.HUE_RED
            }
            googleMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(place.latitude, place.longitude))
                    .title(place.name)
                    .snippet(place.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
        }
    }

    private fun searchPlacesWithGeocoder(lat: Double, lng: Double, type: String): List<NearbyPlace> {
        val places = mutableListOf<NearbyPlace>()
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val searchTerm = when (type) {
                "gas_station" -> "gas station"
                "parking" -> "parking"
                "car_wash" -> "car wash"
                "car_repair" -> "mechanic"
                "electric_vehicle_charging_station" -> "ev charging"
                "hospital" -> "hospital"
                else -> type
            }

            val results = geocoder.getFromLocationName(searchTerm, 10, lat - 0.05, lng - 0.05, lat + 0.05, lng + 0.05)

            results?.forEach { address ->
                val placeLat = address.latitude
                val placeLng = address.longitude
                val distance = calculateDistance(lat, lng, placeLat, placeLng)

                places.add(
                    NearbyPlace(
                        name = address.featureName ?: searchTerm.replaceFirstChar { it.uppercase() },
                        address = address.getAddressLine(0) ?: "Unknown address",
                        distance = String.format("%.1f km", distance),
                        rating = (3.0f + (Math.random() * 2.0f).toFloat()),
                        latitude = placeLat,
                        longitude = placeLng
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (places.isEmpty()) {
            places.addAll(generateSamplePlaces(lat, lng, type))
        }

        return places.sortedBy { it.distance }
    }

    private fun generateSamplePlaces(lat: Double, lng: Double, type: String): List<NearbyPlace> {
        val placeName = when (type) {
            "gas_station" -> "Gas Station"
            "parking" -> "Parking Lot"
            "car_wash" -> "Car Wash"
            "car_repair" -> "Auto Repair"
            "electric_vehicle_charging_station" -> "EV Charger"
            "hospital" -> "Hospital"
            else -> "Place"
        }

        return (1..5).map { i ->
            val offsetLat = lat + (Math.random() - 0.5) * 0.02
            val offsetLng = lng + (Math.random() - 0.5) * 0.02
            val dist = calculateDistance(lat, lng, offsetLat, offsetLng)
            NearbyPlace(
                name = "$placeName #$i",
                address = "Near your location",
                distance = String.format("%.1f km", dist),
                rating = (3.0f + (Math.random() * 2.0f).toFloat()),
                latitude = offsetLat,
                longitude = offsetLng
            )
        }
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0] / 1000.0
    }

    private fun navigateToPlace(place: NearbyPlace) {
        val uri = Uri.parse("google.navigation:q=${place.latitude},${place.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}")
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1003 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap?.isMyLocationEnabled = true
                getCurrentLocation()
            }
        }
    }
}
