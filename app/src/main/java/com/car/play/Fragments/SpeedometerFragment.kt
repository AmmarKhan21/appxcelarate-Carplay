package com.car.play.android.app.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.car.play.android.app.databinding.FragmentSpeedometerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class SpeedometerFragment : Fragment() {

    private lateinit var binding: FragmentSpeedometerBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private var isTracking = false
    private var isHudMode = false
    private var maxSpeed = 0f
    private var totalSpeed = 0f
    private var speedReadings = 0
    private var totalDistance = 0f
    private var lastLocation: Location? = null
    private var startTimeMillis = 0L
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            startTracking()
        } else {
            Toast.makeText(requireContext(), "Location permission required for speedometer", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpeedometerBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnStartStop.setOnClickListener {
            if (isTracking) {
                stopTracking()
            } else {
                checkPermissionsAndStart()
            }
        }

        binding.btnHud.setOnClickListener { toggleHudMode() }
    }

    private fun toggleHudMode() {
        isHudMode = !isHudMode
        if (isHudMode) {
            binding.rootLayout.scaleX = -1f
            binding.tvHudLabel.text = "HUD ON"
        } else {
            binding.rootLayout.scaleX = 1f
            binding.tvHudLabel.text = "HUD"
        }
    }

    private fun checkPermissionsAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startTracking()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        isTracking = true
        maxSpeed = 0f
        totalSpeed = 0f
        speedReadings = 0
        totalDistance = 0f
        lastLocation = null
        startTimeMillis = System.currentTimeMillis()

        binding.btnStartStop.text = "Stop Trip"
        binding.btnStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), com.car.play.android.app.R.color.red))

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location -> updateSpeed(location) }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
        startTimer()
    }

    private fun stopTracking() {
        isTracking = false
        binding.btnStartStop.text = "Start Trip"
        binding.btnStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), com.car.play.android.app.R.color.orange))

        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        stopTimer()
    }

    private fun updateSpeed(location: Location) {
        val speedKmh = if (location.hasSpeed()) {
            location.speed * 3.6f
        } else {
            0f
        }

        binding.tvSpeed.text = String.format("%.0f", speedKmh)

        if (speedKmh > maxSpeed) {
            maxSpeed = speedKmh
            binding.tvMaxSpeed.text = String.format("%.0f", maxSpeed)
        }

        if (speedKmh > 0) {
            totalSpeed += speedKmh
            speedReadings++
            val avgSpeed = totalSpeed / speedReadings
            binding.tvAvgSpeed.text = String.format("%.0f", avgSpeed)
        }

        lastLocation?.let { prev ->
            val distanceMeters = prev.distanceTo(location)
            totalDistance += distanceMeters
            binding.tvDistance.text = String.format("%.1f", totalDistance / 1000f)
        }
        lastLocation = location
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (!isTracking) return
                val elapsedMillis = System.currentTimeMillis() - startTimeMillis
                val seconds = (elapsedMillis / 1000) % 60
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val hours = elapsedMillis / (1000 * 60 * 60)
                if (hours > 0) {
                    binding.tvDuration.text = String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    binding.tvDuration.text = String.format("%02d:%02d", minutes, seconds)
                }
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTracking()
    }
}
