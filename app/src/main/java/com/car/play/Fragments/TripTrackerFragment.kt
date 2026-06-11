package com.car.play.android.app.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.car.play.GoogleAds.GoogleAds
import com.car.play.android.app.Adapters.TripAdapter
import com.car.play.android.app.databinding.FragmentTripTrackerBinding
import com.car.play.android.app.db.TripEntity
import com.car.play.android.app.db.TripViewModel
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class TripTrackerFragment : Fragment() {

    private lateinit var binding: FragmentTripTrackerBinding
    private lateinit var googleAds: GoogleAds
    private lateinit var viewModel: TripViewModel
    private lateinit var tripAdapter: TripAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isTracking = false
    private var startTime: Long = 0L
    private var totalDistance: Double = 0.0
    private var lastLocation: Location? = null
    private var startAddress: String = ""
    private var endAddress: String = ""
    private var maxSpeed: Double = 0.0
    private var speedSum: Double = 0.0
    private var speedCount: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                updateDurationDisplay(elapsed)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!isTracking) return
            val location = result.lastLocation ?: return

            lastLocation?.let { prev ->
                val dist = prev.distanceTo(location) / 1000.0
                totalDistance += dist

                val speed = location.speed * 3.6
                if (speed > maxSpeed) maxSpeed = speed
                speedSum += speed
                speedCount++

                binding.tvCurrentDistance.text = String.format("%.2f km", totalDistance)
                binding.tvCurrentSpeed.text = String.format("%.0f km/h", if (speedCount > 0) speedSum / speedCount else 0.0)
            }

            lastLocation = location
        }
    }

    private lateinit var locationRequest: LocationRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTripTrackerBinding.inflate(inflater, container, false)
        googleAds = GoogleAds()
        googleAds.CheckNative(this, binding.nativeAd)
        viewModel = ViewModelProvider(requireActivity()).get(TripViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        setupRecyclerView()
        setupButtons()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(mutableListOf()) { trip -> viewModel.deleteTrip(trip) }
        binding.rvTrips.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrips.adapter = tripAdapter
    }

    private fun setupButtons() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnStartStop.setOnClickListener {
            if (isTracking) stopTrip() else startTrip()
        }
    }

    private fun observeData() {
        viewModel.allTrips.observe(viewLifecycleOwner, Observer { trips ->
            tripAdapter.updateList(trips)
        })

        viewModel.totalTrips.observe(viewLifecycleOwner, Observer { count ->
            binding.tvTotalTrips.text = (count ?: 0).toString()
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer { dist ->
            binding.tvTotalDistance.text = String.format("%.1f km", dist ?: 0.0)
        })

        viewModel.allTrips.observe(viewLifecycleOwner, Observer { trips ->
            val totalDur = trips.sumOf { it.duration }
            val hours = totalDur / 3600000
            val minutes = (totalDur % 3600000) / 60000
            binding.tvTotalDuration.text = "${hours}h ${minutes}m"
        })
    }

    private fun startTrip() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        isTracking = true
        startTime = SystemClock.elapsedRealtime()
        totalDistance = 0.0
        lastLocation = null
        maxSpeed = 0.0
        speedSum = 0.0
        speedCount = 0
        startAddress = ""
        endAddress = ""

        binding.btnStartStop.text = "Stop Trip"
        binding.btnStartStop.setBackgroundColor(0xFFFF0000.toInt())
        binding.tvTripStatus.text = "Trip in Progress"
        binding.activeTripInfo.visibility = View.VISIBLE
        binding.tvCurrentDistance.text = "0.00 km"
        binding.tvCurrentDuration.text = "00:00"
        binding.tvCurrentSpeed.text = "0 km/h"

        handler.post(timerRunnable)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                lastLocation = it
                startAddress = getAddressFromLocation(it.latitude, it.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopTrip() {
        isTracking = false
        handler.removeCallbacks(timerRunnable)
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val duration = SystemClock.elapsedRealtime() - startTime
        val avgSpeed = if (speedCount > 0) speedSum / speedCount else 0.0

        lastLocation?.let {
            endAddress = getAddressFromLocation(it.latitude, it.longitude)
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = dateFormat.format(Date())

        viewModel.addTrip(
            startTime = System.currentTimeMillis() - duration,
            endTime = System.currentTimeMillis(),
            distance = totalDistance,
            duration = duration,
            averageSpeed = avgSpeed,
            maxSpeed = maxSpeed,
            startAddress = startAddress,
            endAddress = endAddress,
            routePoints = "",
            date = date
        )

        binding.btnStartStop.text = "Start Trip"
        binding.btnStartStop.setBackgroundColor(0xFF008000.toInt())
        binding.tvTripStatus.text = "No Active Trip"
        binding.activeTripInfo.visibility = View.GONE
    }

    private fun updateDurationDisplay(elapsedMs: Long) {
        val seconds = (elapsedMs / 1000) % 60
        val minutes = (elapsedMs / 60000) % 60
        val hours = elapsedMs / 3600000
        binding.tvCurrentDuration.text = if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)?.take(30) ?: "Unknown"
            } else "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isTracking) {
            handler.removeCallbacks(timerRunnable)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrip()
        }
    }
}
