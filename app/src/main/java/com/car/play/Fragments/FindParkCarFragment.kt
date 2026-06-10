package com.car.play.android.app.Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.car.play.GoogleAds.GoogleAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.car.play.android.app.R
import com.car.play.android.app.databinding.FragmentFindParkCarBinding
import com.car.play.android.app.sharepref.MyPref

class FindParkCarFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleAds: GoogleAds
    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var latitude : String = ""
    private var longitude : String = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
   private val binding by lazy { FragmentFindParkCarBinding.inflate(layoutInflater) }
    private val mController by lazy {
        (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleAds=GoogleAds()
        setupClickEvent()
        fetchSharePref()
        loadBanner()
        return binding.root
    }

    private fun fetchSharePref() {
         latitude = MyPref(requireContext()).getLatitude()
         longitude = MyPref(requireContext()).getLongitude()

    }

    private fun loadBanner() {
        googleAds.CheckBanner(this, binding.bannerAd)
    }

    private fun setupClickEvent() {
        binding.icBack.setOnClickListener {  mController.popBackStack()}
        binding.lvFindCar.setOnClickListener { setCarLocation() }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initViews()
    }

    private fun initViews() {
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.fr_find_map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    try {

                        val currentLocation = LatLng(it.latitude, it.longitude)
                        googleMap.clear()
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(currentLocation)
                                .title("You are here")
                        )
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    } catch (e: Exception) {
                        Log.e("FindParkCarFragment", "Error setting marker icon", e)
                    }
                }
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setCarLocation() {
        if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
            try {
                val carLocation = LatLng(latitude.toDouble(), longitude.toDouble())
                val smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(
                    getScaledBitmap(requireContext(), R.drawable.iv_location_car, 200, 110)
                )
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(carLocation)
                        .title("Car is parked here")
                        .icon(smallMarkerIcon)
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
            } catch (e: Exception) {
                Log.e("FindParkCarFragment", "Error setting car location", e)
            }
        } else {
            Toast.makeText(requireContext(), "No Car Parked", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                openAppSettings()

            }
        }
    }
    private fun getScaledBitmap(context: Context, resId: Int, width: Int, height: Int): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }
    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
